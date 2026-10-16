package com.hatsune.lingolens.infrastructure.oss;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.common.exception.BizException;
import io.minio.BucketExistsArgs;
import io.minio.GetBucketPolicyArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Service
public class MinioOssService implements OssService {

    private static final String DEFAULT_OBJECT_PREFIX = "common";
    private static final String POLICY_VERSION = "2012-10-17";
    private static final String PUBLIC_READ_POLICY_SID = "LingoLensPublicReadPrefix";
    private static final String GET_OBJECT_ACTION = "s3:GetObject";

    private final MinioClient minioClient;
    private final String bucket;
    private final String publicUrlPrefix;
    private final boolean ensurePublicReadPolicy;
    private final String publicReadObjectPrefix;
    private volatile boolean publicReadPolicyReady;

    public MinioOssService(@Value("${lingolens.oss.minio.endpoint}") String endpoint,
                           @Value("${lingolens.oss.minio.access-key}") String accessKey,
                           @Value("${lingolens.oss.minio.secret-key}") String secretKey,
                           @Value("${lingolens.oss.minio.bucket}") String bucket,
                           @Value("${lingolens.oss.minio.public-url-prefix}") String publicUrlPrefix,
                           @Value("${lingolens.oss.minio.ensure-public-read-policy:true}") boolean ensurePublicReadPolicy,
                           @Value("${lingolens.oss.minio.public-read-object-prefix:public}") String publicReadObjectPrefix) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
        this.publicUrlPrefix = trimRight(publicUrlPrefix, "/");
        this.ensurePublicReadPolicy = ensurePublicReadPolicy;
        this.publicReadObjectPrefix = resolveObjectPrefix(publicReadObjectPrefix);
    }

    @Override
    public String upload(OssUploadCommand command) {
        try {
            ensureBucketExists();
            byte[] content = decodeBase64(command.getBase64Content());
            String contentType = resolveContentType(command);
            String objectName = buildObjectName(command, contentType);
            ensurePublicReadIfNecessary(command, objectName);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(content), (long) content.length, -1L)
                    .contentType(contentType)
                    .build());
            String url = publicUrlPrefix + "/" + objectName;
            log.info("minio upload success, bucket={}, objectName={}, size={}", bucket, objectName, content.length);
            return url;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("minio upload failed, bucket={}", bucket, e);
            throw new BizException("file upload failed");
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("minio bucket created, bucket={}", bucket);
        }
    }

    private void ensurePublicReadIfNecessary(OssUploadCommand command, String objectName) throws Exception {
        if (!Boolean.TRUE.equals(command.getPublicRead())) {
            return;
        }
        if (!isUnderPublicReadPrefix(objectName)) {
            throw new BizException("public file object prefix must start with " + publicReadObjectPrefix);
        }
        if (!ensurePublicReadPolicy || publicReadPolicyReady) {
            return;
        }
        synchronized (this) {
            if (publicReadPolicyReady) {
                return;
            }
            String policy = buildPublicReadPolicyConfig();
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(policy)
                    .build());
            publicReadPolicyReady = true;
            log.info("minio public read policy ensured, bucket={}, prefix={}", bucket, publicReadObjectPrefix);
        }
    }

    private byte[] decodeBase64(String base64Content) {
        if (!StringUtils.hasText(base64Content)) {
            throw new BizException("file must not be blank");
        }
        String content = base64Content.trim();
        int commaIndex = content.indexOf(',');
        if (commaIndex >= 0) {
            content = content.substring(commaIndex + 1);
        }
        try {
            return Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            throw new BizException("file base64 format is invalid");
        }
    }

    private String buildObjectName(OssUploadCommand command, String contentType) {
        return resolveObjectPrefix(command.getObjectPrefix()) + "/" + UUID.randomUUID() + resolveExtension(command, contentType);
    }

    private String resolveContentType(OssUploadCommand command) {
        String contentType = normalizeContentType(command.getContentType());
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        contentType = normalizeContentType(resolveContentTypeFromDataUrl(command.getBase64Content()));
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        return "application/octet-stream";
    }

    private String resolveObjectPrefix(String objectPrefix) {
        if (!StringUtils.hasText(objectPrefix)) {
            return DEFAULT_OBJECT_PREFIX;
        }
        String prefix = objectPrefix.trim().replace('\\', '/');
        while (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        prefix = prefix.replaceAll("[^A-Za-z0-9/_-]", "_");
        if (!StringUtils.hasText(prefix)) {
            return DEFAULT_OBJECT_PREFIX;
        }
        return prefix;
    }

    private String resolveExtension(OssUploadCommand command, String contentType) {
        String fileName = command.getFileName();
        if (StringUtils.hasText(fileName) && fileName.contains(".")) {
            String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
            if (extension.matches("\\.[a-z0-9]{1,12}")) {
                return extension;
            }
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return ".gif";
        }
        if ("audio/mpeg".equalsIgnoreCase(contentType)) {
            return ".mp3";
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return ".pdf";
        }
        return ".bin";
    }

    private boolean isUnderPublicReadPrefix(String objectName) {
        return objectName.equals(publicReadObjectPrefix) || objectName.startsWith(publicReadObjectPrefix + "/");
    }

    private String buildPublicReadPolicyConfig() {
        JSONObject policy = parseBucketPolicy(readBucketPolicy());
        policy.putIfAbsent("Version", POLICY_VERSION);
        JSONArray statements = policy.getJSONArray("Statement");
        if (statements == null) {
            statements = new JSONArray();
            policy.put("Statement", statements);
        }
        String resource = "arn:aws:s3:::" + bucket + "/" + publicReadObjectPrefix + "/*";
        if (!hasPublicReadStatement(statements, resource)) {
            statements.add(buildPublicReadStatement(resource));
        }
        return JSON.toJSONString(policy);
    }

    private String readBucketPolicy() {
        try {
            return minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucket).build());
        } catch (Exception e) {
            log.info("minio bucket policy read skipped, bucket={}, reason={}", bucket, e.getMessage());
            return null;
        }
    }

    private JSONObject parseBucketPolicy(String policyText) {
        if (!StringUtils.hasText(policyText)) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(policyText);
        } catch (Exception e) {
            log.warn("minio bucket policy parse failed, bucket={}, reason={}", bucket, e.getMessage());
            return new JSONObject();
        }
    }

    private boolean hasPublicReadStatement(JSONArray statements, String resource) {
        for (Object item : statements) {
            if (!(item instanceof JSONObject statement)) {
                continue;
            }
            if (!PUBLIC_READ_POLICY_SID.equals(statement.getString("Sid"))) {
                continue;
            }
            statement.put("Effect", "Allow");
            statement.put("Principal", buildPublicPrincipal());
            statement.put("Action", buildStringArray(GET_OBJECT_ACTION));
            statement.put("Resource", buildStringArray(resource));
            return true;
        }
        return false;
    }

    private JSONObject buildPublicReadStatement(String resource) {
        JSONObject statement = new JSONObject();
        statement.put("Sid", PUBLIC_READ_POLICY_SID);
        statement.put("Effect", "Allow");
        statement.put("Principal", buildPublicPrincipal());
        statement.put("Action", buildStringArray(GET_OBJECT_ACTION));
        statement.put("Resource", buildStringArray(resource));
        return statement;
    }

    private JSONObject buildPublicPrincipal() {
        JSONObject principal = new JSONObject();
        principal.put("AWS", buildStringArray("*"));
        return principal;
    }

    private JSONArray buildStringArray(String value) {
        JSONArray array = new JSONArray();
        array.add(value);
        return array;
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        int semicolonIndex = normalized.indexOf(';');
        if (semicolonIndex >= 0) {
            normalized = normalized.substring(0, semicolonIndex).trim();
        }
        if ("image/jpg".equals(normalized)) {
            return "image/jpeg";
        }
        return normalized;
    }

    private String resolveContentTypeFromDataUrl(String base64Content) {
        if (!StringUtils.hasText(base64Content)) {
            return null;
        }
        String content = base64Content.trim();
        if (!content.startsWith("data:")) {
            return null;
        }
        int semicolonIndex = content.indexOf(';');
        if (semicolonIndex <= "data:".length()) {
            return null;
        }
        return content.substring("data:".length(), semicolonIndex);
    }

    private String trimRight(String text, String suffix) {
        if (text == null || !text.endsWith(suffix)) {
            return text;
        }
        return text.substring(0, text.length() - suffix.length());
    }
}
