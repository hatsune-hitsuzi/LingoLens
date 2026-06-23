package com.hatsune.lingolens.module.user.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.dto.UserPreferenceUpdateRequest;
import com.hatsune.lingolens.module.user.mapper.UserMapper;
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

    private static final String DEFAULT_SOURCE_LANGUAGE = "zh-CN";
    private static final String DEFAULT_TARGET_LANGUAGE = "en";
    private static final String DEFAULT_UI_LANGUAGE = "zh-CN";
    private static final String DEFAULT_THEME_MODE = "system";

    private final UserMapper userMapper;
    private final LanguagePairCatalog languagePairCatalog;

    public UserPreferenceDO getPreference(Long userPkId) {
        UserDO user = userMapper.selectByPrimaryKey(UserDO.builder()
                .id(userPkId)
                .build());
        if (user == null) {
            return defaultPreference(userPkId);
        }
        return UserPreferenceDO.builder()
                .id(user.getId())
                .userPkId(userPkId)
                .sourceLanguage(defaultIfBlank(user.getSourceLanguage(), DEFAULT_SOURCE_LANGUAGE))
                .targetLanguage(defaultIfBlank(user.getTargetLanguage(), DEFAULT_TARGET_LANGUAGE))
                .uiLanguage(defaultIfBlank(user.getUiLanguage(), defaultIfBlank(user.getSourceLanguage(), DEFAULT_UI_LANGUAGE)))
                .themeMode(defaultIfBlank(user.getThemeMode(), DEFAULT_THEME_MODE))
                .build();
    }

    public UserPreferenceDO updatePreference(Long userPkId, UserPreferenceUpdateRequest request) {
        normalizeLanguagePair(request);
        UserDO current = userMapper.selectByPrimaryKey(UserDO.builder()
                .id(userPkId)
                .build());
        if (current == null) {
            return defaultPreference(userPkId);
        }
        UserDO update = UserDO.builder()
                .id(userPkId)
                .sourceLanguage(StringUtils.hasText(request.getSourceLanguage()) ? request.getSourceLanguage() : null)
                .targetLanguage(StringUtils.hasText(request.getTargetLanguage()) ? request.getTargetLanguage() : null)
                .uiLanguage(StringUtils.hasText(request.getUiLanguage()) ? request.getUiLanguage() : null)
                .themeMode(StringUtils.hasText(request.getThemeMode()) ? request.getThemeMode() : null)
                .updateTime(OffsetDateTime.now())
                .build();
        userMapper.updateByPrimaryKey(update);
        return UserPreferenceDO.builder()
                .id(userPkId)
                .userPkId(userPkId)
                .sourceLanguage(defaultIfBlank(update.getSourceLanguage(), current.getSourceLanguage()))
                .targetLanguage(defaultIfBlank(update.getTargetLanguage(), current.getTargetLanguage()))
                .uiLanguage(defaultIfBlank(update.getUiLanguage(), defaultIfBlank(current.getUiLanguage(), current.getSourceLanguage())))
                .themeMode(defaultIfBlank(update.getThemeMode(), current.getThemeMode()))
                .build();
    }

    private UserPreferenceDO defaultPreference(Long userPkId) {
        return UserPreferenceDO.builder()
                .id(userPkId)
                .userPkId(userPkId)
                .sourceLanguage(DEFAULT_SOURCE_LANGUAGE)
                .targetLanguage(DEFAULT_TARGET_LANGUAGE)
                .uiLanguage(DEFAULT_UI_LANGUAGE)
                .themeMode(DEFAULT_THEME_MODE)
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
