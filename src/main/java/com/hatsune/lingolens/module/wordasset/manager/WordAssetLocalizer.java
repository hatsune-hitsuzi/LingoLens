package com.hatsune.lingolens.module.wordasset.manager;

import com.hatsune.lingolens.module.lens.bo.LensWordInfoBO;
import com.hatsune.lingolens.module.lens.dto.LensWordDetailRequest;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.manager.UserPreferenceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Runtime projection from a stable word key to the user's current language pair.
 *
 * @author hatsune_hitsuzi
 * @since 2026-05-27
 */
@Component
@RequiredArgsConstructor
public class WordAssetLocalizer {

    private final WordAssetManager wordAssetManager;
    private final UserPreferenceManager userPreferenceManager;

    public LensWordInfoBO localizeForUser(Long userPkId, String word) {
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        LensWordDetailRequest request = new LensWordDetailRequest();
        request.setWord(word);
        request.setSourceLanguage(preference.getSourceLanguage());
        request.setTargetLanguage(preference.getTargetLanguage());
        return wordAssetManager.resolveWord(request);
    }

    public LensWordInfoBO localizeFastForUser(Long userPkId, String word) {
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        return wordAssetManager.resolveWordFast(
                word,
                preference.getSourceLanguage(),
                preference.getTargetLanguage()
        );
    }

    public LensWordInfoBO localizeImageForUser(Long userPkId, String imageUrl) {
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        return wordAssetManager.resolveImage(
                imageUrl,
                preference.getSourceLanguage(),
                preference.getTargetLanguage()
        );
    }

    public LensWordInfoBO localizeForUser(Long userPkId, Long wordAssetId) {
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        return wordAssetManager.resolveWordByAssetId(
                wordAssetId,
                preference.getSourceLanguage(),
                preference.getTargetLanguage()
        );
    }
}
