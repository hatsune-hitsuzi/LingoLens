package com.hatsune.lingolens.module.wordasset.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Component
public class PronunciationUrlBuilder {

    private final String dictVoiceBaseUrl;
    private final String dictVoiceType;

    public PronunciationUrlBuilder(
            @Value("${lingolens.pronunciation.dictvoice.base-url:http://dict.youdao.com/dictvoice}") String dictVoiceBaseUrl,
            @Value("${lingolens.pronunciation.dictvoice.type:2}") String dictVoiceType) {
        this.dictVoiceBaseUrl = dictVoiceBaseUrl;
        this.dictVoiceType = dictVoiceType;
    }

    public String build(String word) {
        return build(word, "en");
    }

    public String build(String word, String language) {
        return buildFromPath(buildPath(word, language));
    }

    public String buildFromPath(String pronunciationPath) {
        return buildFromPath(dictVoiceBaseUrl, pronunciationPath);
    }

    public String buildFromPath(String baseUrl, String pronunciationPath) {
        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(pronunciationPath)) {
            return "";
        }
        String normalizedBaseUrl = baseUrl.trim();
        String normalizedPath = pronunciationPath.trim();
        if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://")) {
            return normalizedPath;
        }
        if (normalizedPath.startsWith("?") || normalizedPath.startsWith("&")) {
            return normalizedBaseUrl + normalizedPath;
        }
        if (normalizedBaseUrl.endsWith("/") || normalizedPath.startsWith("/")) {
            return normalizedBaseUrl + normalizedPath;
        }
        return normalizedBaseUrl + "/" + normalizedPath;
    }

    public String baseUrl() {
        return dictVoiceBaseUrl;
    }

    public String buildPath(String word, String language) {
        if (!StringUtils.hasText(word)) {
            return "";
        }
        String encodedWord = URLEncoder.encode(word.trim(), StandardCharsets.UTF_8).replace("+", "%20");
        String separator = dictVoiceBaseUrl.contains("?") ? "&" : "?";
        String normalizedLanguage = StringUtils.hasText(language) ? language.trim().toLowerCase(Locale.ROOT) : "en";
        if (normalizedLanguage.startsWith("ja")) {
            return separator + "le=jap&audio=" + encodedWord + "&type=3";
        }
        if (normalizedLanguage.startsWith("zh")) {
            return separator + "le=zh&audio=" + encodedWord + "&type=2";
        }
        return separator + "audio=" + encodedWord + "&type=" + dictVoiceType;
    }
}
