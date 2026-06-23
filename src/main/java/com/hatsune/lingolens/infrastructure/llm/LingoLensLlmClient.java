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
import java.time.Instant;
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
            log.warn("llm request skipped, enabled={}, endpointConfigured={}, authorizationConfigured={}",
                    properties.isEnabled(),
                    StringUtils.hasText(properties.endpoint()),
                    StringUtils.hasText(properties.authorizationHeader()));
            return null;
        }
        try {
            JSONObject request = buildRequest(systemPrompt, userPrompt, imageUrl);
            Instant start = Instant.now();
            log.info("llm request start, model={}, endpoint={}, stream={}, imagePresent={}, systemPromptLength={}, userPromptLength={}",
                    properties.getModel(),
                    properties.endpoint(),
                    request.getBooleanValue("stream"),
                    StringUtils.hasText(imageUrl),
                    safeLength(systemPrompt),
                    safeLength(userPrompt));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.endpoint()))
                    .timeout(Duration.ofSeconds(normalizeTimeout(properties.getRequestTimeoutSeconds(), 90)))
                    .header("Content-Type", "application/json")
                    .header("Accept", properties.isStream() ? "text/event-stream" : "application/json")
                    .header("Authorization", properties.authorizationHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(request.toJSONString()))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long elapsedMillis = Duration.between(start, Instant.now()).toMillis();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("llm request failed, status={}, elapsedMillis={}, body={}",
                        response.statusCode(), elapsedMillis, response.body());
                return null;
            }
            JSONObject parsed = parseChatJson(response.body());
            log.info("llm request success, status={}, elapsedMillis={}, contentSnippet={}",
                    response.statusCode(), elapsedMillis, snippet(chatContent(response.body())));
            return parsed;
        } catch (Exception e) {
            log.warn("llm request failed", e);
            return null;
        }
    }

    public JSONArray completeJsonArray(String systemPrompt, String userPrompt, String imageUrl) {
        if (!available()) {
            log.warn("llm grounding request skipped, enabled={}, endpointConfigured={}, authorizationConfigured={}",
                    properties.isEnabled(),
                    StringUtils.hasText(properties.endpoint()),
                    StringUtils.hasText(properties.authorizationHeader()));
            return new JSONArray();
        }
        try {
            JSONObject request = buildRequest(systemPrompt, userPrompt, imageUrl);
            request.put("response_format", Map.of("type", "json_object"));
            request.put("stream", false);
            request.put("temperature", 0.0D);
            request.put("max_tokens", 1500);
            Instant start = Instant.now();
            log.info("llm grounding request start, model={}, endpoint={}, imagePresent={}, maxTokens={}, temperature={}, systemPromptLength={}, userPromptLength={}",
                    properties.getModel(),
                    properties.endpoint(),
                    StringUtils.hasText(imageUrl),
                    request.getInteger("max_tokens"),
                    request.getDouble("temperature"),
                    safeLength(systemPrompt),
                    safeLength(userPrompt));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.endpoint()))
                    .timeout(Duration.ofSeconds(normalizeTimeout(properties.getRequestTimeoutSeconds(), 90)))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", properties.authorizationHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(request.toJSONString()))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long elapsedMillis = Duration.between(start, Instant.now()).toMillis();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("llm grounding request failed, status={}, elapsedMillis={}, body={}",
                        response.statusCode(), elapsedMillis, response.body());
                return new JSONArray();
            }
            String content = chatContent(response.body());
            JSONArray parsed = parseChatJsonArray(response.body());
            log.info("llm grounding request success, status={}, elapsedMillis={}, parsedCount={}, contentSnippet={}",
                    response.statusCode(), elapsedMillis, parsed.size(), snippet(content));
            if (!StringUtils.hasText(content)) {
                log.warn("llm grounding response content empty, rawBodySnippet={}", snippet(response.body()));
            }
            return parsed;
        } catch (Exception e) {
            log.warn("llm grounding request failed", e);
            return new JSONArray();
        }
    }

    public void completeTextLineStream(String systemPrompt,
                                       String userPrompt,
                                       String imageUrl,
                                       Consumer<String> onLine) {
        if (!available() || onLine == null) {
            log.warn("llm stream request skipped, enabled={}, endpointConfigured={}, authorizationConfigured={}, hasLineConsumer={}",
                    properties.isEnabled(),
                    StringUtils.hasText(properties.endpoint()),
                    StringUtils.hasText(properties.authorizationHeader()),
                    onLine != null);
            return;
        }
        try {
            JSONObject request = buildStreamRequest(systemPrompt, userPrompt, imageUrl);
            log.info("llm stream request start, model={}, endpoint={}, imagePresent={}, systemPromptLength={}, userPromptLength={}",
                    properties.getModel(),
                    properties.endpoint(),
                    StringUtils.hasText(imageUrl),
                    safeLength(systemPrompt),
                    safeLength(userPrompt));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.endpoint()))
                    .timeout(Duration.ofSeconds(normalizeTimeout(properties.getStreamTimeoutSeconds(), 90)))
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
        String content = chatContent(responseBody);
        if (!StringUtils.hasText(content)) {
            return null;
        }
        return JSON.parseObject(stripJsonFence(content));
    }

    private JSONArray parseChatJsonArray(String responseBody) {
        String content = stripJsonFence(chatContent(responseBody));
        if (!StringUtils.hasText(content)) {
            return new JSONArray();
        }
        if (content.startsWith("[") || content.contains("[")) {
            JSONArray array = parsePossiblyTruncatedJsonArray(content);
            if (array != null) {
                return array;
            }
        }
        try {
            JSONObject wrapper = JSON.parseObject(content);
            JSONArray nodes = wrapper.getJSONArray("nodes");
            if (nodes == null) {
                nodes = wrapper.getJSONArray("items");
            }
            if (nodes != null) {
                return nodes;
            }
            String nodesText = wrapper.getString("nodes");
            if (StringUtils.hasText(nodesText)) {
                JSONArray repaired = parsePossiblyTruncatedJsonArray(nodesText);
                return repaired == null ? new JSONArray() : repaired;
            }
        } catch (Exception e) {
            JSONArray repaired = parsePossiblyTruncatedJsonArray(content);
            if (repaired != null) {
                return repaired;
            }
            log.warn("llm grounding json array parse failed, contentSnippet={}", snippet(content), e);
        }
        return new JSONArray();
    }

    private JSONArray parsePossiblyTruncatedJsonArray(String content) {
        String repaired = repairJsonArray(content);
        if (!StringUtils.hasText(repaired)) {
            return null;
        }
        try {
            return JSON.parseArray(repaired);
        } catch (Exception e) {
            log.warn("llm grounding repaired json array parse failed, repairedSnippet={}", snippet(repaired), e);
            return null;
        }
    }

    private String repairJsonArray(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String trimmed = content.trim();
        int arrayStart = trimmed.indexOf('[');
        if (arrayStart < 0) {
            return "";
        }
        String source = trimmed.substring(arrayStart);
        StringBuilder json = new StringBuilder();
        List<Character> stack = new java.util.ArrayList<>();
        boolean inString = false;
        boolean escaping = false;
        boolean arrayStarted = false;
        for (int index = 0; index < source.length(); index++) {
            char ch = source.charAt(index);
            json.append(ch);
            if (escaping) {
                escaping = false;
                continue;
            }
            if (ch == '\\' && inString) {
                escaping = true;
                continue;
            }
            if (ch == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (ch == '[' || ch == '{') {
                stack.add(ch);
                arrayStarted = true;
                continue;
            }
            if (ch == ']' || ch == '}') {
                popMatching(stack, ch);
                if (arrayStarted && stack.isEmpty()) {
                    return json.toString();
                }
            }
        }
        if (!arrayStarted) {
            return "";
        }
        if (inString) {
            json.append('"');
        }
        for (int index = stack.size() - 1; index >= 0; index--) {
            char opening = stack.get(index);
            json.append(opening == '{' ? '}' : ']');
        }
        return json.toString();
    }

    private void popMatching(List<Character> stack, char closing) {
        if (stack.isEmpty()) {
            return;
        }
        char expectedOpening = closing == '}' ? '{' : '[';
        for (int index = stack.size() - 1; index >= 0; index--) {
            char opening = stack.get(index);
            stack.remove(index);
            if (opening == expectedOpening) {
                return;
            }
        }
    }

    private String chatContent(String responseBody) {
        JSONObject body = JSON.parseObject(responseBody);
        JSONArray choices = body.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            return firstText(body.get("content"), body.get("output_text"), body.get("text"));
        }
        JSONObject choice = choices.getJSONObject(0);
        JSONObject message = choice.getJSONObject("message");
        if (message != null) {
            String content = extractContent(message.get("content"));
            if (StringUtils.hasText(content)) {
                return content;
            }
            content = firstText(
                    message.get("output_text"),
                    message.get("text"),
                    message.get("reasoning_content")
            );
            if (StringUtils.hasText(content)) {
                return content;
            }
        }
        return firstText(choice.get("text"), choice.get("content"));
    }

    private String firstText(Object... values) {
        for (Object value : values) {
            String text = extractContent(value);
            if (StringUtils.hasText(text)) {
                return text;
            }
        }
        return "";
    }

    private String extractContent(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String text) {
            return StringUtils.hasText(text) ? text : "";
        }
        if (value instanceof JSONArray array) {
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < array.size(); index++) {
                String itemText = extractContent(array.get(index));
                if (StringUtils.hasText(itemText)) {
                    builder.append(itemText);
                }
            }
            return builder.toString();
        }
        if (value instanceof JSONObject object) {
            String text = object.getString("text");
            if (StringUtils.hasText(text)) {
                return text;
            }
            text = object.getString("content");
            if (StringUtils.hasText(text)) {
                return text;
            }
            text = object.getString("value");
            return StringUtils.hasText(text) ? text : "";
        }
        return "";
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

    private int normalizeTimeout(int timeoutSeconds, int defaultValue) {
        if (timeoutSeconds < 5) {
            return defaultValue;
        }
        return Math.min(timeoutSeconds, 180);
    }

    private int safeLength(String value) {
        return value == null ? 0 : value.length();
    }

    private String snippet(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() <= 500 ? compact : compact.substring(0, 500) + "...";
    }
}
