package com.hatsune.lingolens.module.lens.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.infrastructure.llm.LingoLensLlmClient;
import com.hatsune.lingolens.module.lens.dto.LensGroundingNodeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LensGroundingManager {

    private static final int MAX_NODES = 6;
    private static final String SYSTEM_PROMPT = """
        You are minimaxai/minimax-m3 working as an ultra-fast structured multimodal grounding gateway.

        Goal:
        Identify AT MOST 6 prominent, clear, distinct physical objects in the image.
        Return lightweight trilingual labels and normalized polygons immediately.

        Output schema:
        {
          "nodes": [
            {
              "id": "stable-lowercase-english-object-key",
              "polygon": [[10,20],[35,18],[38,46],[12,48]],
              "labels": {
                "en": "figurine",
                "zh": "手办",
                "ja": "フィギュア"
              }
            }
          ]
        }

        Rules:
        - id is an English lowercase object noun, for example "figurine", "bottle", "keyboard".
        - polygon coordinates are normalized percentages from 0 to 100.
        - labels.en is English, labels.zh is Simplified Chinese, labels.ja is Japanese.
        - Ignore background noise and trivial details.
        - Output raw JSON object only. No markdown, prose, or chain-of-thought.
        - The first character must be "{".
        """;

    private final LingoLensLlmClient llmClient;

    public List<LensGroundingNodeDTO> recognize(String imageReference,
                                                String sourceLanguage,
                                                String targetLanguage) {
        log.info("lens grounding llm recognize start, sourceLanguage={}, targetLanguage={}, imagePresent={}",
                sourceLanguage, targetLanguage, StringUtils.hasText(imageReference));
        String userPrompt = """
                Analyze this frozen camera frame now.
                Return at most 6 prominent physical objects with polygon, English/Chinese/Japanese labels, and compact phonetics.
                
                REMINDER: Follow the System instructions.
                NO prose. NO explanations. NO Markdown blocks.
                The first output character must be '{'.
                """;
        JSONArray generated = llmClient.completeJsonArray(
                SYSTEM_PROMPT,
                userPrompt,
                imageReference);
        Map<String, LensGroundingNodeDTO> deduplicated = new LinkedHashMap<>();
        for (int index = 0; index < generated.size() && deduplicated.size() < MAX_NODES; index++) {
            JSONObject item = generated.getJSONObject(index);
            LensGroundingNodeDTO node = normalizeNode(item, sourceLanguage, targetLanguage);
            if (node != null) {
                deduplicated.put(node.getId(), node);
            }
        }
        log.info("lens grounding llm recognize finished, rawCount={}, acceptedCount={}, ids={}",
                generated.size(),
                deduplicated.size(),
                deduplicated.keySet());
        return List.copyOf(deduplicated.values());
    }

    private LensGroundingNodeDTO normalizeNode(JSONObject item, String sourceLanguage, String targetLanguage) {
        if (item == null) {
            return null;
        }
        Map<String, String> labels = normalizeStringMap(item.getJSONObject("labels"));
        Map<String, String> phonetics = normalizeStringMap(item.getJSONObject("phonetics"));
        String id = normalizeId(defaultText(item.getString("id"), labels.get("en")));
        List<List<Double>> polygon = normalizePolygon(item.getJSONArray("polygon"));
        if (!StringUtils.hasText(id)
                || polygon.size() < 3) {
            log.debug("lens grounding node dropped, id={}, polygonSize={}", id, polygon.size());
            return null;
        }
        return LensGroundingNodeDTO.builder()
                .id(id)
                .polygon(polygon)
                .learningWord(languageValue(labels, targetLanguage, id))
                .learningPhonetic(languageValue(phonetics, targetLanguage, ""))
                .nativeTranslation(languageValue(labels, sourceLanguage, id))
                .labels(labels)
                .phonetics(phonetics)
                .build();
    }

    private Map<String, String> normalizeStringMap(JSONObject object) {
        Map<String, String> result = new LinkedHashMap<>();
        if (object == null) {
            return result;
        }
        for (String key : List.of("en", "zh", "ja")) {
            String value = trim(object.getString(key));
            if (StringUtils.hasText(value)) {
                result.put(key, value);
            }
        }
        return result;
    }

    private String languageValue(Map<String, String> values, String language, String fallback) {
        if (values == null || values.isEmpty()) {
            return fallback;
        }
        String key = contentLanguage(language);
        String value = values.get(key);
        if (StringUtils.hasText(value)) {
            return value;
        }
        value = values.get("en");
        return StringUtils.hasText(value) ? value : fallback;
    }

    private List<List<Double>> normalizePolygon(JSONArray polygon) {
        if (polygon == null) {
            return List.of();
        }
        List<List<Double>> points = new ArrayList<>();
        for (int index = 0; index < polygon.size() && points.size() < 80; index++) {
            JSONArray point = polygon.getJSONArray(index);
            if (point == null || point.size() < 2) {
                continue;
            }
            Double x = point.getDouble(0);
            Double y = point.getDouble(1);
            if (x == null || y == null || !Double.isFinite(x) || !Double.isFinite(y)) {
                continue;
            }
            points.add(List.of(clampPercent(x), clampPercent(y)));
        }
        return points;
    }

    private double clampPercent(double value) {
        return Math.max(0D, Math.min(100D, value));
    }

    private String normalizeId(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String contentLanguage(String language) {
        if (!StringUtils.hasText(language)) {
            return "en";
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("zh") || normalized.contains("chinese")) {
            return "zh";
        }
        if (normalized.startsWith("ja") || normalized.startsWith("jp") || normalized.contains("japanese")) {
            return "ja";
        }
        return "en";
    }
}
