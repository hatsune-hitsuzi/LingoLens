package com.hatsune.lingolens.module.oss.service.impl;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.oss.dto.OssUploadRequest;
import com.hatsune.lingolens.module.oss.dto.OssUploadResponse;
import com.hatsune.lingolens.module.oss.manager.OssManager;
import com.hatsune.lingolens.module.oss.service.OssUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class OssUploadServiceImpl implements OssUploadService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_HEADER = "token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final OssManager ossManager;
    private final CommonTools commonTools;
    private final StringRedisTemplate stringRedisTemplate;
    private final HttpServletRequest httpServletRequest;

    @Override
    public OssUploadResponse upload(@RequestBody String requestJson) {
        log.info("oss upload request, requestJson={}", commonTools.maskSensitiveJson(requestJson, "fileBase64"));
        try {
            OssUploadRequest request = commonTools.parseJson(requestJson, OssUploadRequest.class);
            commonTools.validateByJsr303(request);
            commonTools.validateUploadRequest(request);
            Long ownerId = resolveOwnerId();
            String url = ossManager.upload(request, ownerId);
            return buildSuccessResponse(url);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("oss upload failed, reason={}", e.getMessage());
            return buildFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("oss upload failed by system error", e);
            return buildFailResponse("system busy, please try again later");
        }
    }

    @Override
    public OssUploadResponse uploadPublicImage(@RequestBody String requestJson) {
        log.info("oss public image upload request, requestJson={}", commonTools.maskSensitiveJson(requestJson, "fileBase64"));
        try {
            OssUploadRequest request = commonTools.parseJson(requestJson, OssUploadRequest.class);
            commonTools.validateByJsr303(request);
            commonTools.validateUploadRequest(request);
            String url = ossManager.uploadPublicImage(request);
            return buildSuccessResponse(url);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("oss public image upload failed, reason={}", e.getMessage());
            return buildFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("oss public image upload failed by system error", e);
            return buildFailResponse("system busy, please try again later");
        }
    }

    private Long resolveOwnerId() {
        String token = resolveToken();
        if (!StringUtils.hasText(token)) {
            log.info("oss upload owner resolve skipped, reason=token is blank");
            return null;
        }
        String ownerIdValue = stringRedisTemplate.opsForValue().get(RedisKeyConstant.AUTH_TOKEN.key(token));
        if (!StringUtils.hasText(ownerIdValue)) {
            log.warn("oss upload owner resolve failed, reason=token not found");
            return null;
        }
        try {
            return Long.valueOf(ownerIdValue);
        } catch (NumberFormatException e) {
            log.warn("oss upload owner resolve failed, reason=owner id invalid, ownerId={}", ownerIdValue);
            return null;
        }
    }

    private String resolveToken() {
        String authorization = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return httpServletRequest.getHeader(TOKEN_HEADER);
    }

    private OssUploadResponse buildSuccessResponse(String url) {
        return OssUploadResponse.builder()
                .result(ResponseResult.success())
                .url(url)
                .build();
    }

    private OssUploadResponse buildFailResponse(String message) {
        return OssUploadResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }
}
