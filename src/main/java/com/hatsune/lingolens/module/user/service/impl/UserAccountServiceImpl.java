package com.hatsune.lingolens.module.user.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.lock.RedisDistributedLock;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.common.response.BaseResponse;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.dto.UserLogoutRequest;
import com.hatsune.lingolens.module.user.dto.UserPasswordUpdateRequest;
import com.hatsune.lingolens.module.user.dto.UserProfileInfoRequest;
import com.hatsune.lingolens.module.user.dto.UserProfileResponse;
import com.hatsune.lingolens.module.user.dto.UserProfileUpdateRequest;
import com.hatsune.lingolens.module.user.manager.UserAccountManager;
import com.hatsune.lingolens.module.user.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private static final Duration UPDATE_NICKNAME_LOCK_TTL = Duration.ofSeconds(10);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_HEADER = "token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserAccountManager userAccountManager;
    private final RedisDistributedLock redisDistributedLock;
    private final CommonTools commonTools;
    private final HttpServletRequest httpServletRequest;

    @Override
    public BaseResponse logout(@RequestBody String requestJson) {
        log.info("user logout request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            commonTools.parseJsonAllowBlank(requestJson, UserLogoutRequest.class);
            String token = resolveToken();
            Assert.hasText(token, "token must not be blank");
            userAccountManager.logout(token);
            return BaseResponse.success();
        } catch (BizException | IllegalArgumentException e) {
            log.warn("user logout failed, reason={}", e.getMessage());
            return BaseResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("user logout failed by system error", e);
            return BaseResponse.fail("system busy, please try again later");
        }
    }

    @Override
    public UserProfileResponse profileInfo(@RequestBody String requestJson) {
        log.info("user profile info request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            commonTools.parseJsonAllowBlank(requestJson, UserProfileInfoRequest.class);
            UserDO user = userAccountManager.getProfile(UserContext.getUserId());
            return buildProfileSuccessResponse(user);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("user profile info failed, reason={}", e.getMessage());
            return buildProfileFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("user profile info failed by system error", e);
            return buildProfileFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateProfile(@RequestBody String requestJson) {
        log.info("user profile update request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            UserProfileUpdateRequest request = commonTools.parseJson(requestJson, UserProfileUpdateRequest.class);
            commonTools.validateByJsr303(request);
            normalizeProfileUpdateRequest(request);
            validateProfileUpdateRequest(request);
            Long id = UserContext.getUserId();
            UserDO user;
            if (StringUtils.hasText(request.getNickname())) {
                user = redisDistributedLock.execute(RedisKeyConstant.USER_UPDATE_NICKNAME_LOCK, UPDATE_NICKNAME_LOCK_TTL,
                        () -> userAccountManager.updateProfile(id, request), request.getNickname());
            } else {
                user = userAccountManager.updateProfile(id, request);
            }
            return buildProfileSuccessResponse(user);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("user profile update failed, reason={}", e.getMessage());
            return buildProfileFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("user profile update failed by system error", e);
            return buildProfileFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse updatePassword(@RequestBody String requestJson) {
        log.info("user password update request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            UserPasswordUpdateRequest request = commonTools.parseJson(requestJson, UserPasswordUpdateRequest.class);
            commonTools.validateByJsr303(request);
            validatePasswordUpdateRequest(request);
            Long id = UserContext.getUserId();
            userAccountManager.updatePassword(id, request);
            userAccountManager.logout(resolveToken());
            return BaseResponse.success();
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("user password update failed, reason={}", e.getMessage());
            return BaseResponse.fail(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("user password update failed by system error", e);
            return BaseResponse.fail("system busy, please try again later");
        }
    }

    private void normalizeProfileUpdateRequest(UserProfileUpdateRequest request) {
        if (request.getNickname() != null) {
            request.setNickname(request.getNickname().trim());
        }
        if (request.getAvatarUrl() != null) {
            request.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getEmail() != null) {
            request.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getBio() != null) {
            request.setBio(request.getBio().trim());
        }
    }

    private void validateProfileUpdateRequest(UserProfileUpdateRequest request) {
        Assert.notNull(request, "profile update request must not be null");
        Assert.isTrue(StringUtils.hasText(request.getNickname())
                        || StringUtils.hasText(request.getAvatarUrl())
                        || StringUtils.hasText(request.getEmail())
                        || request.getBio() != null,
                "nickname, avatar url, email or bio must not be blank");
        if (request.getNickname() != null) {
            Assert.hasText(request.getNickname(), "nickname must not be blank");
            Assert.isTrue(request.getNickname().length() >= 2 && request.getNickname().length() <= 32,
                    "nickname size must be between 2 and 32");
            Assert.isTrue(!request.getNickname().matches("\\d{8}"), "nickname cannot be an 8-digit number");
        }
        if (request.getAvatarUrl() != null) {
            Assert.hasText(request.getAvatarUrl(), "avatar url must not be blank");
        }
        if (request.getEmail() != null) {
            Assert.hasText(request.getEmail(), "email must not be blank");
        }
    }

    private void validatePasswordUpdateRequest(UserPasswordUpdateRequest request) {
        Assert.notNull(request, "password update request must not be null");
        Assert.isTrue(!request.getOldPassword().equals(request.getNewPassword()),
                "new password cannot be the same as old password");
        Assert.isTrue(request.getNewPassword().equals(request.getConfirmPassword()),
                "confirm password is different from new password");
    }

    private String resolveToken() {
        String authorization = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return httpServletRequest.getHeader(TOKEN_HEADER);
    }

    private UserProfileResponse buildProfileSuccessResponse(UserDO user) {
        return UserProfileResponse.builder()
                .result(ResponseResult.success())
                .id(user.getId())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .bio(user.getBio())
                .build();
    }

    private UserProfileResponse buildProfileFailResponse(String message) {
        return UserProfileResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }
}
