package com.hatsune.lingolens.infrastructure.llm;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.config.LingoLensLlmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Thin, provider-tolerant image-edit client. It prefers OpenAI-style
 * image-edit responses but also accepts common url/base64 wrapper shapes.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LingoLensImageEditClient {

    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final LingoLensLlmProperties properties;

    public boolean available() {
        return properties.imageEditAvailable();
    }

    public ImageEditResult editImage(String prompt, String imageUrl) {
        if (!available() || !StringUtils.hasText(prompt) || !StringUtils.hasText(imageUrl)) {
            log.info("llm image edit skipped, available={}, promptPresent={}, imagePresent={}",
                    available(), StringUtils.hasText(prompt), StringUtils.hasText(imageUrl));
            return null;
        }
        try {
            JSONObject request = buildRequest(prompt, imageUrl);
            Instant start = Instant.now();
            log.info("llm image edit request start, model={}, endpoint={}, promptLength={}, imagePresent={}",
                    properties.imageEditModel(),
                    properties.imageEditEndpoint(),
                    prompt.length(),
                    true);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.imageEditEndpoint()))
                    .timeout(Duration.ofSeconds(normalizeTimeout(properties.getImageEditTimeoutSeconds())))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", properties.imageEditAuthorizationHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(request.toJSONString()))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long elapsedMillis = Duration.between(start, Instant.now()).toMillis();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("llm image edit request failed, status={}, elapsedMillis={}, bodySnippet={}",
                        response.statusCode(), elapsedMillis, snippet(response.body()));
                return null;
            }
            ImageEditResult result = parseResult(response.body());
            if (result != null && result.hasImageUrl() && !result.hasBase64Content()) {
                result = downloadImageUrl(result.getImageUrl());
            }
            log.info("llm image edit request success, status={}, elapsedMillis={}, hasBase64={}, hasUrl={}",
                    response.statusCode(),
                    elapsedMillis,
                    result != null && result.hasBase64Content(),
                    result != null && result.hasImageUrl());
            return result;
        } catch (Exception e) {
            log.warn("llm image edit request failed", e);
            return null;
        }
    }

    private JSONObject buildRequest(String prompt, String imageUrl) {
        JSONObject request = new JSONObject();
        request.put("model", properties.imageEditModel());
        request.put("prompt", prompt);
        request.put("image", imageUrl.trim());
        request.put("image_url", imageUrl.trim());
        request.put("size", StringUtils.hasText(properties.getImageEditSize())
                ? properties.getImageEditSize().trim()
                : "1024x1024");
        request.put("n", 1);
        request.put("response_format", "b64_json");
        return request;
    }

    private ImageEditResult parseResult(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        JSONObject body = JSONObject.parseObject(responseBody);
        ImageEditResult result = parseImageObject(body);
        if (result != null) {
            return result;
        }
        JSONArray data = body.getJSONArray("data");
        if (data != null && !data.isEmpty()) {
            result = parseImageObject(data.getJSONObject(0));
            if (result != null) {
                return result;
            }
        }
        JSONArray images = body.getJSONArray("images");
        if (images != null && !images.isEmpty()) {
            Object first = images.get(0);
            if (first instanceof JSONObject object) {
                return parseImageObject(object);
            }
            String value = String.valueOf(first);
            if (looksLikeUrl(value)) {
                return ImageEditResult.builder().imageUrl(value).contentType(DEFAULT_CONTENT_TYPE).build();
            }
            if (StringUtils.hasText(value)) {
                return ImageEditResult.builder().base64Content(stripDataUrl(value)).contentType(DEFAULT_CONTENT_TYPE).build();
            }
        }
        return null;
    }

    private ImageEditResult parseImageObject(JSONObject object) {
        if (object == null || object.isEmpty()) {
            return null;
        }
        String base64 = firstText(
                object.getString("b64_json"),
                object.getString("base64"),
                object.getString("image_base64"),
                object.getString("image"));
        if (StringUtils.hasText(base64) && !looksLikeUrl(base64)) {
            return ImageEditResult.builder()
                    .base64Content(stripDataUrl(base64))
                    .contentType(contentTypeFromDataUrl(base64))
                    .build();
        }
        String url = firstText(
                object.getString("url"),
                object.getString("image_url"),
                object.getString("output_url"),
                object.getString("image"));
        if (looksLikeUrl(url)) {
            return ImageEditResult.builder()
                    .imageUrl(url.trim())
                    .contentType(DEFAULT_CONTENT_TYPE)
                    .build();
        }
        return null;
    }

    private ImageEditResult downloadImageUrl(String imageUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl.trim()))
                    .timeout(Duration.ofSeconds(45))
                    .header("Accept", "image/*")
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null) {
                return ImageEditResult.builder().imageUrl(imageUrl).contentType(DEFAULT_CONTENT_TYPE).build();
            }
            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .filter(StringUtils::hasText)
                    .orElse(DEFAULT_CONTENT_TYPE);
            return ImageEditResult.builder()
                    .base64Content(Base64.getEncoder().encodeToString(response.body()))
                    .contentType(contentType)
                    .imageUrl(imageUrl)
                    .build();
        } catch (Exception e) {
            log.warn("llm image edit output download skipped, imageUrl={}", imageUrl, e);
            return ImageEditResult.builder().imageUrl(imageUrl).contentType(DEFAULT_CONTENT_TYPE).build();
        }
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean looksLikeUrl(String value) {
        return StringUtils.hasText(value)
                && (value.startsWith("http://") || value.startsWith("https://"));
    }

    private String stripDataUrl(String value) {
        String text = value.trim();
        int commaIndex = text.indexOf(',');
        return commaIndex >= 0 ? text.substring(commaIndex + 1) : text;
    }

    private String contentTypeFromDataUrl(String value) {
        String text = value == null ? "" : value.trim();
        if (text.startsWith("data:") && text.contains(";base64")) {
            return text.substring("data:".length(), text.indexOf(";base64"));
        }
        return DEFAULT_CONTENT_TYPE;
    }

    private int normalizeTimeout(int timeoutSeconds) {
        if (timeoutSeconds < 10) {
            return 120;
        }
        return Math.min(timeoutSeconds, 300);
    }

    private String snippet(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() <= 500 ? compact : compact.substring(0, 500) + "...";
    }
}
