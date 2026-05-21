package com.hatsune.lingolens.module.user.service.impl;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.lock.RedisDistributedLock;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.dto.LoginRequest;
import com.hatsune.lingolens.module.user.dto.LoginResponse;
import com.hatsune.lingolens.module.user.dto.RegisterRequest;
import com.hatsune.lingolens.module.user.dto.RegisterResponse;
import com.hatsune.lingolens.module.user.manager.UserAuthManager;
import com.hatsune.lingolens.module.user.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private static final Duration REGISTER_LOCK_TTL = Duration.ofSeconds(10);

    private final UserAuthManager userAuthManager;
    private final RedisDistributedLock redisDistributedLock;
    private final CommonTools commonTools;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResponse register(@RequestBody String requestJson) {
        log.info("user register request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            RegisterRequest request = commonTools.parseJson(requestJson, RegisterRequest.class);
            commonTools.validateByJsr303(request);
            commonTools.validateRegisterRequest(request);
            String nickname = request.getNickname().trim();
            request.setNickname(nickname);
            return redisDistributedLock.execute(RedisKeyConstant.USER_REGISTER_NICKNAME_LOCK, REGISTER_LOCK_TTL, () -> {
                if (userAuthManager.nicknameExists(nickname)) {
                    log.warn("user register failed, nickname already exists, nickname={}", nickname);
                    return buildRegisterFailResponse("nickname already exists");
                }
                userAuthManager.registerUser(request);
                return buildRegisterSuccessResponse();
            }, nickname);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("user register failed, reason={}", e.getMessage());
            return buildRegisterFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("user register failed by system error", e);
            return buildRegisterFailResponse("system busy, please try again later");
        }
    }

    @Override
    public LoginResponse login(@RequestBody String requestJson) {
        log.info("user login request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            LoginRequest request = commonTools.parseJson(requestJson, LoginRequest.class);
            commonTools.validateByJsr303(request);
            commonTools.validateLoginRequest(request);
            request.setAccount(request.getAccount().trim());
            UserDO user = userAuthManager.findByAccount(request.getAccount()).orElse(null);
            if (user == null || !userAuthManager.passwordMatches(request.getPassword(), user.getPasswordHash())) {
                log.warn("user login failed, account={}, reason=account or password is invalid", request.getAccount());
                return buildLoginFailResponse("account or password is invalid");
            }
            String token = userAuthManager.issueToken(user.getId());
            return buildLoginSuccessResponse(token);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("user login failed, reason={}", e.getMessage());
            return buildLoginFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("user login failed by system error", e);
            return buildLoginFailResponse("system busy, please try again later");
        }
    }

    private RegisterResponse buildRegisterSuccessResponse() {
        return RegisterResponse.builder()
                .result(ResponseResult.success())
                .build();
    }

    private RegisterResponse buildRegisterFailResponse(String message) {
        return RegisterResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private LoginResponse buildLoginSuccessResponse(String token) {
        return LoginResponse.builder()
                .result(ResponseResult.success())
                .token(token)
                .tokenExpireAt(Instant.now().plus(userAuthManager.tokenTtl()).getEpochSecond())
                .build();
    }

    private LoginResponse buildLoginFailResponse(String message) {
        return LoginResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }
}
