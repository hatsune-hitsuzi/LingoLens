package com.hatsune.lingolens.module.user.support;

import com.hatsune.lingolens.module.user.dto.LanguagePairDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Component
public class LanguagePairCatalog {

    private final List<LanguagePairDTO> supportedPairs = List.of(
            pair("zh-CN", "en", "中 -> 英"),
            pair("zh-CN", "ja", "中 -> 日"),
            pair("ja", "zh-CN", "日 -> 中"),
            pair("ja", "en", "日 -> 英"),
            pair("en", "ja", "英 -> 日"),
            pair("en", "zh-CN", "英 -> 中")
    );

    private final Map<String, LanguagePairDTO> pairByDirection = supportedPairs.stream()
            .collect(java.util.stream.Collectors.toUnmodifiableMap(LanguagePairDTO::getDirection, pair -> pair));

    public List<LanguagePairDTO> supportedPairs() {
        return supportedPairs;
    }

    public LanguagePairDTO defaultPair() {
        return pairByDirection.get("zh-CN-en");
    }

    public Optional<LanguagePairDTO> find(String sourceLanguage, String targetLanguage) {
        return supportedPairs.stream()
                .filter(pair -> pair.getSourceLanguage().equals(sourceLanguage)
                        && pair.getTargetLanguage().equals(targetLanguage))
                .findFirst();
    }

    public Optional<LanguagePairDTO> findByDirection(String direction) {
        return Optional.ofNullable(pairByDirection.get(direction));
    }

    public String direction(String sourceLanguage, String targetLanguage) {
        return sourceLanguage + "-" + targetLanguage;
    }

    private static LanguagePairDTO pair(String sourceLanguage, String targetLanguage, String label) {
        return LanguagePairDTO.builder()
                .direction(sourceLanguage + "-" + targetLanguage)
                .sourceLanguage(sourceLanguage)
                .targetLanguage(targetLanguage)
                .label(label)
                .build();
    }
}
