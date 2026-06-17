package com.hatsune.lingolens.infrastructure.llm;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.config.LingoLensLlmProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Minimal OpenAI-compatible chat client used by NVIDIA Integrate and similar
 * providers. The caller owns prompt design and JSON interpretation.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LingoLensLlmClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final LingoLensLlmProperties properties;

    public boolean available() {
        return properties.isEnabled()
                && StringUtils.hasText(properties.endpoint())
                && StringUtils.hasText(properties.authorizationHeader());
    }

    public JSONObject completeJson(String systemPrompt, String userPrompt) {
        return completeJson(systemPrompt, userPrompt, null);
    }

    public JSONObject completeJson(String systemPrompt, String userPrompt, String imageUrl) {
        if (!available()) {
            return null;
        }
        try {
            JSONObject request = buildRequest(systemPrompt, userPrompt, imageUrl);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.endpoint()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", properties.isStream() ? "text/event-stream" : "application/json")
                    .header("Authorization", properties.authorizationHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(request.toJSONString()))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("llm request failed, status={}, body={}", response.statusCode(), response.body());
                return null;
            }
            return parseChatJson(response.body());
        } catch (Exception e) {
            log.warn("llm request failed", e);
            return null;
        }
    }

    public void completeTextLineStream(String systemPrompt,
                                       String userPrompt,
                                       String imageUrl,
                                       Consumer<String> onLine) {
        if (!available() || onLine == null) {
            return;
        }
        try {
            JSONObject request = buildStreamRequest(systemPrompt, userPrompt, imageUrl);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.endpoint()))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .header("Authorization", properties.authorizationHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(request.toJSONString()))
                    .build();
            HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("llm stream request failed, status={}", response.statusCode());
                return;
            }
            streamSseContentLines(response.body(), onLine);
        } catch (Exception e) {
            log.warn("llm stream request failed", e);
        }
    }

    private JSONObject buildRequest(String systemPrompt, String userPrompt, String imageUrl) {
        JSONObject request = new JSONObject();
        request.put("model", properties.getModel());
        request.put("max_tokens", properties.getMaxTokens());
        request.put("temperature", properties.getTemperature());
        request.put("top_p", properties.getTopP());
        request.put("stream", properties.isStream());
        request.put("response_format", Map.of("type", properties.responseFormatType()));
        request.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userContent(userPrompt, imageUrl))
        ));
        return request;
    }

    private JSONObject buildStreamRequest(String systemPrompt, String userPrompt, String imageUrl) {
        JSONObject request = new JSONObject();
        request.put("model", properties.getModel());
        request.put("max_tokens", 200);
        request.put("temperature", 0);
        request.put("top_p", properties.getTopP());
        request.put("stream", true);
        request.put("reasoning_effort", "low");
        request.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userContent(userPrompt, imageUrl))
        ));
        return request;
    }

    private Object userContent(String userPrompt, String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return userPrompt;
        }
        JSONArray content = new JSONArray();
        JSONObject text = new JSONObject();
        text.put("type", "text");
        text.put("text", userPrompt);
        content.add(text);

        JSONObject image = new JSONObject();
        image.put("type", "image_url");
        image.put("image_url", Map.of("url", imageUrl.trim()));
        content.add(image);
        return content;
    }

    private JSONObject parseChatJson(String responseBody) {
        JSONObject body = JSON.parseObject(responseBody);
        JSONArray choices = body.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        if (message == null) {
            return null;
        }
        String content = message.getString("content");
        if (!StringUtils.hasText(content)) {
            return null;
        }
        return JSON.parseObject(stripJsonFence(content));
    }

    private void streamSseContentLines(InputStream body, Consumer<String> onLine) throws Exception {
        StringBuilder contentBuffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
            String sseLine;
            while ((sseLine = reader.readLine()) != null) {
                String delta = parseSseDeltaContent(sseLine);
                if (!StringUtils.hasText(delta)) {
                    continue;
                }
                contentBuffer.append(delta);
                flushCompleteLines(contentBuffer, onLine);
            }
        }
        String tail = contentBuffer.toString().trim();
        if (StringUtils.hasText(tail)) {
            onLine.accept(tail);
        }
    }

    private String parseSseDeltaContent(String sseLine) {
        if (!StringUtils.hasText(sseLine) || !sseLine.startsWith("data:")) {
            return "";
        }
        String payload = sseLine.substring("data:".length()).trim();
        if (!StringUtils.hasText(payload) || "[DONE]".equals(payload)) {
            return "";
        }
        try {
            JSONObject body = JSON.parseObject(payload);
            JSONArray choices = body.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "";
            }
            JSONObject choice = choices.getJSONObject(0);
            JSONObject delta = choice.getJSONObject("delta");
            if (delta != null) {
                return delta.getString("content");
            }
            JSONObject message = choice.getJSONObject("message");
            return message == null ? "" : message.getString("content");
        } catch (Exception e) {
            return "";
        }
    }

    private void flushCompleteLines(StringBuilder buffer, Consumer<String> onLine) {
        int newlineIndex;
        while ((newlineIndex = buffer.indexOf("\n")) >= 0) {
            String line = buffer.substring(0, newlineIndex).trim();
            buffer.delete(0, newlineIndex + 1);
            if (StringUtils.hasText(line)) {
                onLine.accept(line);
            }
        }
    }

    private String stripJsonFence(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }
}
