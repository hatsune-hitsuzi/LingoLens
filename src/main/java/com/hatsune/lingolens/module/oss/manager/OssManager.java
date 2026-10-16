package com.hatsune.lingolens.module.oss.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.infrastructure.oss.OssService;
import com.hatsune.lingolens.infrastructure.oss.OssUploadCommand;
import com.hatsune.lingolens.module.oss.dto.OssUploadRequest;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.manager.UserAuthManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssManager {

    private static final String DEFAULT_BIZ_TYPE = "common";
    private static final String PUBLIC_IMAGE_PREFIX = "public/image-hosting";
    private static final String USER_PREFIX = "lingolens_user_";
    private static final String ANONYMOUS_PREFIX = "lingolens_user_anonymous_";
    private static final int HASH_LENGTH = 16;
    private static final Set<String> PUBLIC_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final OssService ossService;
    private final UserAuthManager userAuthManager;

    public String upload(OssUploadRequest request, Long ownerId) {
        String objectPrefix = buildObjectPrefix(request, ownerId);
        log.info("oss upload object prefix resolved, objectPrefix={}", objectPrefix);
        return ossService.upload(OssUploadCommand.builder()
                .ownerId(ownerId)
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .objectPrefix(objectPrefix)
                .base64Content(request.getFileBase64())
                .build());
    }

    public String uploadPublicImage(OssUploadRequest request) {
        String contentType = resolvePublicImageContentType(request);
        log.info("oss public image upload object prefix resolved, objectPrefix={}", PUBLIC_IMAGE_PREFIX);
        return ossService.upload(OssUploadCommand.builder()
                .fileName(request.getFileName())
                .contentType(contentType)
                .objectPrefix(PUBLIC_IMAGE_PREFIX)
                .base64Content(request.getFileBase64())
                .publicRead(true)
                .build());
    }

    private String buildObjectPrefix(OssUploadRequest request, Long ownerId) {
        String ownerPrefix = buildOwnerPrefix(request, ownerId);
        return ownerPrefix + "/" + resolveBizType(request);
    }

    private String resolvePublicImageContentType(OssUploadRequest request) {
        String contentType = normalizeContentType(request.getContentType());
        if (!StringUtils.hasText(contentType)) {
            contentType = normalizeContentType(resolveContentTypeFromDataUrl(request.getFileBase64()));
        }
        if (!StringUtils.hasText(contentType)) {
            contentType = resolveContentTypeFromFileName(request.getFileName());
        }
        if (!PUBLIC_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new BizException("only png, jpg, jpeg, webp, gif images are supported");
        }
        return contentType;
    }

    private String buildOwnerPrefix(OssUploadRequest request, Long ownerId) {
        if (ownerId != null) {
            return userAuthManager.findById(ownerId)
                    .map(UserDO::getUserId)
                    .filter(StringUtils::hasText)
                    .map(this::sanitizeOwnerValue)
                    .map(userId -> USER_PREFIX + userId)
                    .orElseGet(() -> ANONYMOUS_PREFIX + hash("missing-user:" + ownerId));
        }
        return ANONYMOUS_PREFIX + hash("anonymous:" + request.getFileBase64());
    }

    private String resolveBizType(OssUploadRequest request) {
        if (StringUtils.hasText(request.getBizType())) {
            return sanitizePrefix(request.getBizType());
        }
        if (StringUtils.hasText(request.getObjectPrefix())) {
            return sanitizePrefix(request.getObjectPrefix());
        }
        return DEFAULT_BIZ_TYPE;
    }

    private String sanitizePrefix(String value) {
        String prefix = value.trim().replace('\\', '/');
        while (prefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        prefix = prefix.replaceAll("[^A-Za-z0-9/_-]", "_");
        if (!StringUtils.hasText(prefix)) {
            return DEFAULT_BIZ_TYPE;
        }
        return prefix;
    }

    private String sanitizeOwnerValue(String value) {
        return value.trim().replaceAll("[^A-Za-z0-9_-]", "_");
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

    private String resolveContentTypeFromDataUrl(String fileBase64) {
        if (!StringUtils.hasText(fileBase64)) {
            return null;
        }
        String content = fileBase64.trim();
        if (!content.startsWith("data:")) {
            return null;
        }
        int semicolonIndex = content.indexOf(';');
        if (semicolonIndex <= "data:".length()) {
            return null;
        }
        return content.substring("data:".length(), semicolonIndex);
    }

    private String resolveContentTypeFromFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        String lowerFileName = fileName.trim().toLowerCase(Locale.ROOT);
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerFileName.endsWith(".png")) {
            return "image/png";
        }
        if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        }
        if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        }
        return null;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(HASH_LENGTH);
            for (int i = 0; i < HASH_LENGTH / 2; i++) {
                builder.append(String.format("%02x", digest[i]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
