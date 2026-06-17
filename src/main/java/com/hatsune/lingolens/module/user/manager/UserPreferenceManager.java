package com.hatsune.lingolens.module.user.manager;

import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.dto.UserPreferenceUpdateRequest;
import com.hatsune.lingolens.module.user.mapper.UserPreferenceMapper;
import com.hatsune.lingolens.module.user.support.LanguagePairCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Component
@RequiredArgsConstructor
public class UserPreferenceManager {

    private static final String USER_PREFERENCE_ID_BIZ_TYPE = "user_preference";
    private static final String DEFAULT_SOURCE_LANGUAGE = "zh-CN";
    private static final String DEFAULT_TARGET_LANGUAGE = "en";
    private static final String DEFAULT_UI_LANGUAGE = "zh-CN";
    private static final String DEFAULT_THEME_MODE = "system";

    private final UserPreferenceMapper userPreferenceMapper;
    private final DistributedIdGenerator distributedIdGenerator;
    private final LanguagePairCatalog languagePairCatalog;

    public UserPreferenceDO getPreference(Long userPkId) {
        UserPreferenceDO preference = userPreferenceMapper.selectOne(UserPreferenceDO.builder()
                .userPkId(userPkId)
                .build());
        if (preference != null) {
            return preference;
        }
        return UserPreferenceDO.builder()
                .userPkId(userPkId)
                .sourceLanguage(DEFAULT_SOURCE_LANGUAGE)
                .targetLanguage(DEFAULT_TARGET_LANGUAGE)
                .uiLanguage(DEFAULT_UI_LANGUAGE)
                .themeMode(DEFAULT_THEME_MODE)
                .build();
    }

    public UserPreferenceDO updatePreference(Long userPkId, UserPreferenceUpdateRequest request) {
        UserPreferenceDO preference = userPreferenceMapper.selectOne(UserPreferenceDO.builder()
                .userPkId(userPkId)
                .build());
        if (preference == null) {
            preference = insertPreference(userPkId, request);
        } else {
            preference = updatePreference(preference, request);
        }
        return preference;
    }

    private UserPreferenceDO insertPreference(Long userPkId, UserPreferenceUpdateRequest request) {
        normalizeLanguagePair(request);
        OffsetDateTime now = OffsetDateTime.now();
        UserPreferenceDO preference = UserPreferenceDO.builder()
                .id(distributedIdGenerator.id(USER_PREFERENCE_ID_BIZ_TYPE))
                .userPkId(userPkId)
                .sourceLanguage(defaultIfBlank(request.getSourceLanguage(), DEFAULT_SOURCE_LANGUAGE))
                .targetLanguage(defaultIfBlank(request.getTargetLanguage(), DEFAULT_TARGET_LANGUAGE))
                .uiLanguage(defaultIfBlank(request.getUiLanguage(), defaultIfBlank(request.getSourceLanguage(), DEFAULT_UI_LANGUAGE)))
                .themeMode(defaultIfBlank(request.getThemeMode(), DEFAULT_THEME_MODE))
                .createTime(now)
                .updateTime(now)
                .build();
        userPreferenceMapper.insert(preference);
        return preference;
    }

    private UserPreferenceDO updatePreference(UserPreferenceDO current, UserPreferenceUpdateRequest request) {
        normalizeLanguagePair(request);
        UserPreferenceDO update = UserPreferenceDO.builder()
                .id(current.getId())
                .sourceLanguage(StringUtils.hasText(request.getSourceLanguage()) ? request.getSourceLanguage() : null)
                .targetLanguage(StringUtils.hasText(request.getTargetLanguage()) ? request.getTargetLanguage() : null)
                .uiLanguage(StringUtils.hasText(request.getUiLanguage()) ? request.getUiLanguage() : null)
                .themeMode(StringUtils.hasText(request.getThemeMode()) ? request.getThemeMode() : null)
                .updateTime(OffsetDateTime.now())
                .build();
        userPreferenceMapper.updateByPrimaryKey(update);
        return UserPreferenceDO.builder()
                .id(current.getId())
                .userPkId(current.getUserPkId())
                .sourceLanguage(defaultIfBlank(update.getSourceLanguage(), current.getSourceLanguage()))
                .targetLanguage(defaultIfBlank(update.getTargetLanguage(), current.getTargetLanguage()))
                .uiLanguage(defaultIfBlank(update.getUiLanguage(), defaultIfBlank(current.getUiLanguage(), current.getSourceLanguage())))
                .themeMode(defaultIfBlank(update.getThemeMode(), current.getThemeMode()))
                .build();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private void normalizeLanguagePair(UserPreferenceUpdateRequest request) {
        if (request == null) {
            return;
        }
        if (StringUtils.hasText(request.getLanguageDirection())) {
            languagePairCatalog.findByDirection(request.getLanguageDirection().trim())
                    .ifPresentOrElse(pair -> {
                        request.setSourceLanguage(pair.getSourceLanguage());
                        request.setTargetLanguage(pair.getTargetLanguage());
                    }, () -> {
                        throw new BizException("unsupported language direction");
                    });
            return;
        }
        boolean hasSource = StringUtils.hasText(request.getSourceLanguage());
        boolean hasTarget = StringUtils.hasText(request.getTargetLanguage());
        if (!hasSource && !hasTarget) {
            return;
        }
        String sourceLanguage = hasSource ? request.getSourceLanguage().trim() : DEFAULT_SOURCE_LANGUAGE;
        String targetLanguage = hasTarget ? request.getTargetLanguage().trim() : DEFAULT_TARGET_LANGUAGE;
        languagePairCatalog.find(sourceLanguage, targetLanguage)
                .orElseThrow(() -> new BizException("unsupported language direction"));
        request.setSourceLanguage(sourceLanguage);
        request.setTargetLanguage(targetLanguage);
    }
}
