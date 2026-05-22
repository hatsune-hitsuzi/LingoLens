package com.hatsune.lingolens.module.user.manager;

import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.dto.UserPreferenceUpdateRequest;
import com.hatsune.lingolens.module.user.mapper.UserPreferenceMapper;
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
    private static final String DEFAULT_SOURCE_LANGUAGE = "en";
    private static final String DEFAULT_TARGET_LANGUAGE = "ja";
    private static final String DEFAULT_THEME_MODE = "system";

    private final UserPreferenceMapper userPreferenceMapper;
    private final DistributedIdGenerator distributedIdGenerator;

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
        OffsetDateTime now = OffsetDateTime.now();
        UserPreferenceDO preference = UserPreferenceDO.builder()
                .id(distributedIdGenerator.id(USER_PREFERENCE_ID_BIZ_TYPE))
                .userPkId(userPkId)
                .sourceLanguage(defaultIfBlank(request.getSourceLanguage(), DEFAULT_SOURCE_LANGUAGE))
                .targetLanguage(defaultIfBlank(request.getTargetLanguage(), DEFAULT_TARGET_LANGUAGE))
                .themeMode(defaultIfBlank(request.getThemeMode(), DEFAULT_THEME_MODE))
                .createTime(now)
                .updateTime(now)
                .build();
        userPreferenceMapper.insert(preference);
        return preference;
    }

    private UserPreferenceDO updatePreference(UserPreferenceDO current, UserPreferenceUpdateRequest request) {
        UserPreferenceDO update = UserPreferenceDO.builder()
                .id(current.getId())
                .sourceLanguage(StringUtils.hasText(request.getSourceLanguage()) ? request.getSourceLanguage() : null)
                .targetLanguage(StringUtils.hasText(request.getTargetLanguage()) ? request.getTargetLanguage() : null)
                .themeMode(StringUtils.hasText(request.getThemeMode()) ? request.getThemeMode() : null)
                .updateTime(OffsetDateTime.now())
                .build();
        userPreferenceMapper.updateByPrimaryKey(update);
        return UserPreferenceDO.builder()
                .id(current.getId())
                .userPkId(current.getUserPkId())
                .sourceLanguage(defaultIfBlank(update.getSourceLanguage(), current.getSourceLanguage()))
                .targetLanguage(defaultIfBlank(update.getTargetLanguage(), current.getTargetLanguage()))
                .themeMode(defaultIfBlank(update.getThemeMode(), current.getThemeMode()))
                .build();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
