package com.hatsune.lingolens.module.user.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.dto.UserPreferenceInfoRequest;
import com.hatsune.lingolens.module.user.dto.UserPreferenceResponse;
import com.hatsune.lingolens.module.user.dto.UserPreferenceUpdateRequest;
import com.hatsune.lingolens.module.user.manager.UserPreferenceManager;
import com.hatsune.lingolens.module.user.service.UserPreferenceService;
import com.hatsune.lingolens.module.user.support.LanguagePairCatalog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceManager userPreferenceManager;
    private final LanguagePairCatalog languagePairCatalog;
    private final CommonTools commonTools;

    @Override
    public UserPreferenceResponse info(@RequestBody String requestJson) {
        log.info("user preference info request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            commonTools.parseJsonAllowBlank(requestJson, UserPreferenceInfoRequest.class);
            UserPreferenceDO preference = userPreferenceManager.getPreference(UserContext.getUserId());
            return buildSuccessResponse(preference);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("user preference info failed, reason={}", e.getMessage());
            return buildFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("user preference info failed by system error", e);
            return buildFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserPreferenceResponse update(@RequestBody String requestJson) {
        log.info("user preference update request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            UserPreferenceUpdateRequest request = commonTools.parseJson(requestJson, UserPreferenceUpdateRequest.class);
            commonTools.validateByJsr303(request);
            normalizeRequest(request);
            validateRequest(request);
            UserPreferenceDO preference = userPreferenceManager.updatePreference(UserContext.getUserId(), request);
            return buildSuccessResponse(preference);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("user preference update failed, reason={}", e.getMessage());
            return buildFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("user preference update failed by system error", e);
            return buildFailResponse("system busy, please try again later");
        }
    }

    private void normalizeRequest(UserPreferenceUpdateRequest request) {
        if (request.getLanguageDirection() != null) {
            request.setLanguageDirection(request.getLanguageDirection().trim());
        }
        if (request.getSourceLanguage() != null) {
            request.setSourceLanguage(request.getSourceLanguage().trim());
        }
        if (request.getTargetLanguage() != null) {
            request.setTargetLanguage(request.getTargetLanguage().trim());
        }
        if (request.getUiLanguage() != null) {
            request.setUiLanguage(request.getUiLanguage().trim());
        }
        if (request.getThemeMode() != null) {
            request.setThemeMode(request.getThemeMode().trim());
        }
    }

    private void validateRequest(UserPreferenceUpdateRequest request) {
        Assert.notNull(request, "user preference update request must not be null");
        Assert.isTrue(StringUtils.hasText(request.getSourceLanguage())
                        || StringUtils.hasText(request.getTargetLanguage())
                        || StringUtils.hasText(request.getLanguageDirection())
                        || StringUtils.hasText(request.getUiLanguage())
                        || StringUtils.hasText(request.getThemeMode()),
                "language direction, source language, target language, ui language or theme mode must not be blank");
    }

    private UserPreferenceResponse buildSuccessResponse(UserPreferenceDO preference) {
        String direction = languagePairCatalog.direction(preference.getSourceLanguage(), preference.getTargetLanguage());
        String directionName = languagePairCatalog.find(preference.getSourceLanguage(), preference.getTargetLanguage())
                .map(pair -> pair.getLabel())
                .orElse(direction);
        return UserPreferenceResponse.builder()
                .result(ResponseResult.success())
                .sourceLanguage(preference.getSourceLanguage())
                .targetLanguage(preference.getTargetLanguage())
                .languageDirection(direction)
                .languageDirectionName(directionName)
                .uiLanguage(StringUtils.hasText(preference.getUiLanguage())
                        ? preference.getUiLanguage()
                        : preference.getSourceLanguage())
                .themeMode(preference.getThemeMode())
                .languagePairs(languagePairCatalog.supportedPairs())
                .build();
    }

    private UserPreferenceResponse buildFailResponse(String message) {
        return UserPreferenceResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }
}
