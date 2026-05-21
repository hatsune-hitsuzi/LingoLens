package com.hatsune.lingolens.common.tools;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.oss.dto.OssUploadRequest;
import com.hatsune.lingolens.module.user.dto.LoginRequest;
import com.hatsune.lingolens.module.user.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonTools {

    private static final int MAX_LOG_PARAM_LENGTH = 1_024;

    private final Validator validator;

    public <T> T parseJson(String requestJson, Class<T> targetType) {
        Assert.hasText(requestJson, "request json must not be blank");
        try {
            return JSON.parseObject(requestJson, targetType);
        } catch (JSONException e) {
            throw new BizException("request json format is invalid");
        }
    }

    public <T> T parseJsonAllowBlank(String requestJson, Class<T> targetType) {
        if (!org.springframework.util.StringUtils.hasText(requestJson)) {
            return newInstance(targetType);
        }
        return parseJson(requestJson, targetType);
    }

    public String maskSensitiveJson(String requestJson, String... longTextFields) {
        if (requestJson == null) {
            return null;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(requestJson);
            maskTextField(jsonObject, "password");
            maskTextField(jsonObject, "passwordHash");
            maskTextField(jsonObject, "oldPassword");
            maskTextField(jsonObject, "newPassword");
            maskTextField(jsonObject, "confirmPassword");
            if (longTextFields != null) {
                for (String fieldName : longTextFields) {
                    maskLongTextField(jsonObject, fieldName);
                }
            }
            return truncateForLog(JSON.toJSONString(jsonObject));
        } catch (JSONException ignored) {
            return truncateForLog(requestJson);
        }
    }

    public <T> void validateByJsr303(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new BizException(message);
        }
    }

    public void validateRegisterRequest(RegisterRequest request) {
        Assert.notNull(request, "register request must not be null");
        Assert.hasText(request.getNickname(), "nickname must not be blank");
        Assert.hasText(request.getPassword(), "password must not be blank");
        Assert.hasText(request.getAvatarUrl(), "avatar url must not be blank");
        Assert.isTrue(!request.getNickname().trim().matches("\\d{8}"),
                "nickname cannot be an 8-digit number");
    }

    public void validateLoginRequest(LoginRequest request) {
        Assert.notNull(request, "login request must not be null");
        Assert.hasText(request.getAccount(), "account must not be blank");
        Assert.hasText(request.getPassword(), "password must not be blank");
    }

    public void validateUploadRequest(OssUploadRequest request) {
        Assert.notNull(request, "oss upload request must not be null");
        Assert.hasText(request.getFileBase64(), "file must not be blank");
    }

    public void markRollbackOnly() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (NoTransactionException ignored) {
            log.debug("no active transaction found when marking rollback only");
        }
    }

    public String truncateForLog(String text) {
        if (text == null || text.length() <= MAX_LOG_PARAM_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_LOG_PARAM_LENGTH) + "...";
    }

    private void maskTextField(JSONObject jsonObject, String fieldName) {
        if (jsonObject.containsKey(fieldName)) {
            jsonObject.put(fieldName, "***");
        }
    }

    private void maskLongTextField(JSONObject jsonObject, String fieldName) {
        String value = jsonObject.getString(fieldName);
        if (value != null) {
            jsonObject.put(fieldName, "[masked,length=" + value.length() + "]");
        }
    }

    private <T> T newInstance(Class<T> targetType) {
        try {
            return targetType.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new BizException("request json format is invalid");
        }
    }
}
