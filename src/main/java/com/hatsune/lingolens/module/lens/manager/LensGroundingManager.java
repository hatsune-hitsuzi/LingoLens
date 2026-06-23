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

    private static final int MAX_NODES = 8;
    private static final String SYSTEM_PROMPT = """
            You are a fast visual grounding assistant for a spatial vocabulary app.
            Analyze the frozen camera photo and return only representative physical objects
            that are useful for memory-palace vocabulary review.

            Requirements:
            1. Return 3-8 clear concrete objects. Avoid large background surfaces such as wall,
               floor, ceiling, table, counter, sky, screen background, or room.
            2. For every object, return a polygon in normalized percentage coordinates [0,100].
               The frontend will draw the polygon directly.
            3. id must be a stable lowercase English object key.
            4. learningWord must use the requested target learning language.
            5. nativeTranslation must use the requested native/source language.
            6. learningPhonetic must contain IPA for English, pinyin for Chinese, or romaji for Japanese.
            7. Deduplicate objects. Return the most central/visible instance when uncertain.

            Output rules:
            Return a raw JSON Array only. No markdown. No explanation. No wrapper object.
            [
              {
                "id": "laptop",
                "polygon": [[10,20],[35,18],[38,46],[12,48]],
                "learningWord": "laptop",
                "learningPhonetic": "/laptop/",
                "nativeTranslation": "notebook computer"
              }
            ]
            """;

    private final LingoLensLlmClient llmClient;

    public List<LensGroundingNodeDTO> recognize(String imageReference,
                                                String sourceLanguage,
                                                String targetLanguage) {
        log.info("lens grounding llm recognize start, sourceLanguage={}, targetLanguage={}, imagePresent={}",
                sourceLanguage, targetLanguage, StringUtils.hasText(imageReference));
        String userPrompt = """
                Analyze this frozen camera frame now.
                Native/source language: %s
                Target learning language: %s
                Return 3-8 clear physical objects using the required JSON Array schema.
                """.formatted(sourceLanguage, targetLanguage);
        JSONArray generated = llmClient.completeJsonArray(
                SYSTEM_PROMPT,
                userPrompt,
                imageReference);
        Map<String, LensGroundingNodeDTO> deduplicated = new LinkedHashMap<>();
        for (int index = 0; index < generated.size() && deduplicated.size() < MAX_NODES; index++) {
            JSONObject item = generated.getJSONObject(index);
            LensGroundingNodeDTO node = normalizeNode(item);
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

    private LensGroundingNodeDTO normalizeNode(JSONObject item) {
        if (item == null) {
            return null;
        }
        String id = normalizeId(item.getString("id"));
        String learningWord = trim(item.getString("learningWord"));
        String nativeTranslation = trim(item.getString("nativeTranslation"));
        List<List<Double>> polygon = normalizePolygon(item.getJSONArray("polygon"));
        if (!StringUtils.hasText(id)
                || !StringUtils.hasText(learningWord)
                || !StringUtils.hasText(nativeTranslation)
                || polygon.size() < 3) {
            log.debug("lens grounding node dropped, id={}, learningWord={}, nativeTranslation={}, polygonSize={}",
                    id, learningWord, nativeTranslation, polygon.size());
            return null;
        }
        return LensGroundingNodeDTO.builder()
                .id(id)
                .polygon(polygon)
                .learningWord(learningWord)
                .learningPhonetic(trim(item.getString("learningPhonetic")))
                .nativeTranslation(nativeTranslation)
                .build();
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
}
