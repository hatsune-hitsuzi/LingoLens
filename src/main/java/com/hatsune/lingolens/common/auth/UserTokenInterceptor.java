package com.hatsune.lingolens.common.auth;

import com.alibaba.fastjson2.JSON;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.common.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserTokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_HEADER = "token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String token = resolveToken(request);
        log.info("auth token check, uri={}, tokenExists={}", request.getRequestURI(), StringUtils.hasText(token));
        if (!StringUtils.hasText(token)) {
            writeAuthFail(response, "login expired, please login again");
            return false;
        }

        String userIdValue = stringRedisTemplate.opsForValue().get(RedisKeyConstant.AUTH_TOKEN.key(token));
        if (!StringUtils.hasText(userIdValue)) {
            log.warn("auth token check failed, uri={}, reason=token not found", request.getRequestURI());
            writeAuthFail(response, "login expired, please login again");
            return false;
        }

        try {
            UserContext.setUserId(Long.valueOf(userIdValue));
            return true;
        } catch (NumberFormatException e) {
            log.error("auth token check failed, invalid user id in redis, value={}", userIdValue);
            writeAuthFail(response, "login expired, please login again");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return request.getHeader(TOKEN_HEADER);
    }

    private void writeAuthFail(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(BaseResponse.fail(message)));
    }
}
