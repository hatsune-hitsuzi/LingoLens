package com.hatsune.lingolens.infrastructure.jwt;

import com.alibaba.fastjson2.JSON;
import com.hatsune.lingolens.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Component
public class JwtTokenProvider {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final SecretKeySpec secretKeySpec;

    public JwtTokenProvider(@Value("${lingolens.jwt.secret:lingolens-default-secret-please-change-in-production}") String secret) {
        Assert.isTrue(secret.length() >= 32, "lingolens.jwt.secret length must be at least 32");
        this.secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
    }

    public String createToken(Long userId, Duration ttl) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + ttl.getSeconds();

        String headerJson = JSON.toJSONString(newLinkedMap(
                "alg", "HS256",
                "typ", "JWT"));
        String payloadJson = JSON.toJSONString(newLinkedMap(
                "sub", String.valueOf(userId),
                "iat", issuedAt,
                "exp", expiresAt));
        String signingInput = encode(headerJson) + "." + encode(payloadJson);
        return signingInput + "." + sign(signingInput);
    }

    private Map<String, Object> newLinkedMap(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }

    private String encode(String json) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception e) {
            throw new BizException("failed to sign jwt token");
        }
    }
}
