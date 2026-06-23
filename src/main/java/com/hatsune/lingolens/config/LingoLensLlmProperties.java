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

    private String groundingInvokeUrl = "";

    private String groundingAuthorization = "";

    private String groundingApiKey = "";

    private String groundingModel = "meta/llama-4-maverick-17b-128e-instruct";

    private int groundingMaxTokens = 1200;

    private double groundingTemperature = 0.0D;

    private String enrichmentInvokeUrl = "";

    private String enrichmentAuthorization = "";

    private String enrichmentApiKey = "";

    private String enrichmentModel = "MiniMax-M3";

    private int enrichmentMaxTokens = 2400;

    private double enrichmentTemperature = 0.1D;

    public String endpoint() {
        return StringUtils.hasText(invokeUrl) ? invokeUrl.trim() : baseUrl.trim();
    }

    public String authorizationHeader() {
        return authorizationHeader(authorization, apiKey);
    }

    public String groundingEndpoint() {
        return endpoint(groundingInvokeUrl);
    }

    public String groundingAuthorizationHeader() {
        return authorizationHeader(groundingAuthorization, groundingApiKey);
    }

    public String groundingModel() {
        return StringUtils.hasText(groundingModel) ? groundingModel.trim() : model;
    }

    public int groundingMaxTokens() {
        return groundingMaxTokens > 0 ? groundingMaxTokens : maxTokens;
    }

    public double groundingTemperature() {
        return groundingTemperature;
    }

    public boolean groundingAvailable() {
        return enabled
                && StringUtils.hasText(groundingEndpoint())
                && StringUtils.hasText(groundingAuthorizationHeader());
    }

    public String enrichmentEndpoint() {
        return endpoint(enrichmentInvokeUrl);
    }

    public String enrichmentAuthorizationHeader() {
        return authorizationHeader(enrichmentAuthorization, enrichmentApiKey);
    }

    public String enrichmentModel() {
        return StringUtils.hasText(enrichmentModel) ? enrichmentModel.trim() : model;
    }

    public int enrichmentMaxTokens() {
        return enrichmentMaxTokens > 0 ? enrichmentMaxTokens : maxTokens;
    }

    public double enrichmentTemperature() {
        return enrichmentTemperature;
    }

    public boolean enrichmentAvailable() {
        return enabled
                && StringUtils.hasText(enrichmentEndpoint())
                && StringUtils.hasText(enrichmentAuthorizationHeader());
    }

    private String endpoint(String profileInvokeUrl) {
        if (StringUtils.hasText(profileInvokeUrl)) {
            return profileInvokeUrl.trim();
        }
        return endpoint();
    }

    private String authorizationHeader(String profileAuthorization, String profileApiKey) {
        if (StringUtils.hasText(profileAuthorization) || StringUtils.hasText(profileApiKey)) {
            return authorizationHeaderValue(profileAuthorization, profileApiKey);
        }
        return authorizationHeaderValue(authorization, apiKey);
    }

    private String authorizationHeaderValue(String authorizationValue, String apiKeyValue) {
        if (StringUtils.hasText(authorizationValue)) {
            String value = authorizationValue.trim();
            return value.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())
                    ? value
                    : "Bearer " + value;
        }
        if (StringUtils.hasText(apiKeyValue)) {
            return "Bearer " + apiKeyValue.trim();
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
