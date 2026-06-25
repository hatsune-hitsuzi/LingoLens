package com.hatsune.lingolens.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Presentation-time defaults for sparse word asset payloads.
 * Database rows should keep unknown generated fields as empty strings.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-24
 */
@Data
@Component
@ConfigurationProperties(prefix = "lingolens.word-asset.defaults")
public class WordAssetDefaultsProperties {

    private Map<String, String> phonetic = new HashMap<>();

    private Map<String, String> pos = new HashMap<>();

    private Map<String, String> definition = new HashMap<>();

    private Map<String, String> example = new HashMap<>();

    public String phonetic(String language) {
        return valueOf(phonetic, language);
    }

    public String pos(String language) {
        return valueOf(pos, language);
    }

    public String definition(String language) {
        return valueOf(definition, language);
    }

    public String example(String language) {
        return valueOf(example, language);
    }

    private String valueOf(Map<String, String> map, String language) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        String key = contentLanguage(language);
        String value = map.get(key);
        if (!StringUtils.hasText(value)) {
            value = map.get(language);
        }
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String contentLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return "en";
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("zh")) {
            return "zh";
        }
        if (normalized.startsWith("ja")) {
            return "ja";
        }
        return "en";
    }
}
