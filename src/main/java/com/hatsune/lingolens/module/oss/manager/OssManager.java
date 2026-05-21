package com.hatsune.lingolens.module.oss.manager;

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

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssManager {

    private static final String DEFAULT_BIZ_TYPE = "common";
    private static final String USER_PREFIX = "lingolens_user_";
    private static final String ANONYMOUS_PREFIX = "lingolens_user_anonymous_";
    private static final int HASH_LENGTH = 16;

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

    private String buildObjectPrefix(OssUploadRequest request, Long ownerId) {
        String ownerPrefix = buildOwnerPrefix(request, ownerId);
        return ownerPrefix + "/" + resolveBizType(request);
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
