package com.hatsune.lingolens.infrastructure.oss;

import com.hatsune.lingolens.common.exception.BizException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Service
public class MinioOssService implements OssService {

    private static final String DEFAULT_OBJECT_PREFIX = "common";

    private final MinioClient minioClient;
    private final String bucket;
    private final String publicUrlPrefix;

    public MinioOssService(@Value("${lingolens.oss.minio.endpoint}") String endpoint,
                           @Value("${lingolens.oss.minio.access-key}") String accessKey,
                           @Value("${lingolens.oss.minio.secret-key}") String secretKey,
                           @Value("${lingolens.oss.minio.bucket}") String bucket,
                           @Value("${lingolens.oss.minio.public-url-prefix}") String publicUrlPrefix) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
        this.publicUrlPrefix = trimRight(publicUrlPrefix, "/");
    }

    @Override
    public String upload(OssUploadCommand command) {
        try {
            ensureBucketExists();
            byte[] content = decodeBase64(command.getBase64Content());
            String contentType = resolveContentType(command);
            String objectName = buildObjectName(command, contentType);
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
        if (StringUtils.hasText(command.getContentType())) {
            return command.getContentType();
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
        if ("audio/mpeg".equalsIgnoreCase(contentType)) {
            return ".mp3";
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return ".pdf";
        }
        return ".bin";
    }

    private String trimRight(String text, String suffix) {
        if (text == null || !text.endsWith(suffix)) {
            return text;
        }
        return text.substring(0, text.length() - suffix.length());
    }
}
