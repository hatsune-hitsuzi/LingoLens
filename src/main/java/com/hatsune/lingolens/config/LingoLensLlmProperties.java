package com.hatsune.lingolens.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * YAML-backed LLM settings. Authorization may be configured as a full header
 * value, for example "Bearer xxx", or as apiKey for backward compatibility.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-15
 */
@Data
@Component
@ConfigurationProperties(prefix = "lingolens.llm")
public class LingoLensLlmProperties {

    private boolean enabled = false;

    private String invokeUrl = "";

    private String baseUrl = "";

    private String authorization = "";

    private String apiKey = "";

    private String model = "stepfun-ai/step-3.7-flash";

    private int maxTokens = 16384;

    private double temperature = 1.0D;

    private double topP = 0.95D;

    private boolean stream = false;

    private int requestTimeoutSeconds = 90;

    private int streamTimeoutSeconds = 90;

    private ResponseFormat responseFormat = new ResponseFormat();

    private List<LanguageSpec> languages = defaultLanguages();

    private String imageSystemPrompt = "";

    private String wordSystemPrompt = "";

    public String endpoint() {
        return StringUtils.hasText(invokeUrl) ? invokeUrl.trim() : baseUrl.trim();
    }

    public String authorizationHeader() {
        if (StringUtils.hasText(authorization)) {
            String value = authorization.trim();
            return value.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())
                    ? value
                    : "Bearer " + value;
        }
        if (StringUtils.hasText(apiKey)) {
            return "Bearer " + apiKey.trim();
        }
        return "";
    }

    public String responseFormatType() {
        return responseFormat == null || !StringUtils.hasText(responseFormat.getType())
                ? "json_object"
                : responseFormat.getType().trim();
    }

    private static List<LanguageSpec> defaultLanguages() {
        List<LanguageSpec> specs = new ArrayList<>();
        specs.add(language("zh", "item_zh", "Chinese"));
        specs.add(language("en", "item_en", "English"));
        specs.add(language("ja", "item_ja", "Japanese"));
        return specs;
    }

    private static LanguageSpec language(String code, String responseKey, String name) {
        LanguageSpec spec = new LanguageSpec();
        spec.setCode(code);
        spec.setResponseKey(responseKey);
        spec.setName(name);
        return spec;
    }

    @Data
    public static class ResponseFormat {
        private String type = "json_object";
    }

    @Data
    public static class LanguageSpec {
        private String code;
        private String responseKey;
        private String name;
    }
}
