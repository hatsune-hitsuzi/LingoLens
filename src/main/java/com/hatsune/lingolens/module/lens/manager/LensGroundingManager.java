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
            System: You are a strict, structured Multimodal Grounding API Gateway.

            CRITICAL LIMITATION:
            Identify AT MOST 6 of the most prominent, clear, and distinct physical objects
            in the image. Do not analyze background noise, minor cables, or trivial details.
            Focus only on the top 6 main subjects.

            SCHEMA:
            [
              {
                "id": "stable-lowercase-english-object-key",
                "polygon": [[10,20],[35,18],[38,46],[12,48]]
              }
            ]

            FIELD RULE:
            id must be the object noun in stable lowercase English, for example
            "figurine", "bottle", "keyboard", "monitor".
            polygon coordinates must be normalized percentages from 0 to 100.
            Do not translate. Do not generate phonetics. Do not generate examples.

            OUTPUT RULE:
            DO NOT think. DO NOT explain. DO NOT output any introduction or chain-of-thought text.
            Your very first output character MUST be '['. Output raw JSON Array matching the
            required schema directly, or the system will crash immediately!
            """;

    private final LingoLensLlmClient llmClient;

    public List<LensGroundingNodeDTO> recognize(String imageReference,
                                                String sourceLanguage,
                                                String targetLanguage) {
        log.info("lens grounding llm recognize start, sourceLanguage={}, targetLanguage={}, imagePresent={}",
                sourceLanguage, targetLanguage, StringUtils.hasText(imageReference));
        String userPrompt = """
                Analyze this frozen camera frame now.
                Return only id and polygon for at most 6 prominent physical objects.
                Do not translate. Do not include phonetics. Do not include examples.
                The first output character must be '['.
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
        List<List<Double>> polygon = normalizePolygon(item.getJSONArray("polygon"));
        if (!StringUtils.hasText(id)
                || polygon.size() < 3) {
            log.debug("lens grounding node dropped, id={}, polygonSize={}", id, polygon.size());
            return null;
        }
        return LensGroundingNodeDTO.builder()
                .id(id)
                .polygon(polygon)
                .learningWord(id)
                .learningPhonetic("")
                .nativeTranslation(id)
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
