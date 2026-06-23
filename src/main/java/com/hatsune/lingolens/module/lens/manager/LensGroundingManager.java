package com.hatsune.lingolens.module.lens.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.hatsune.lingolens.infrastructure.llm.LingoLensLlmClient;
import com.hatsune.lingolens.module.lens.dto.LensGroundingNodeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LensGroundingManager {

    private static final int MAX_NODES = 8;
    private static final String SYSTEM_PROMPT = """
            你是一个精准的视觉定位与双语言语记忆专家。请分析用户拍摄的这张 1024px 高清定格照片。

            【任务要求】
            1. 找出画面中所有最具代表性的、适合用来做空间记忆法背单词的核心实体物品，最多 8 个。
            2. 为每个物品给出精确多边形边界。坐标必须归一化到 [0,100] 百分比坐标系。
            3. id 必须是稳定的英文小写物品标识；learningWord 使用目标学习语言；nativeTranslation 使用母语。
            4. learningPhonetic 统一承载英语 IPA、中文拼音或日语罗马音。
            5. 同一个物品只返回一次，不返回墙壁、地板、天空等大面积环境背景。

            【严格输出】
            只能返回纯 JSON Array，不要 Markdown、解释或前后导言：
            [
              {
                "id": "laptop",
                "polygon": [[10,20],[35,18],[38,46],[12,48]],
                "learningWord": "laptop",
                "learningPhonetic": "/ˈlæptɒp/",
                "nativeTranslation": "笔记本电脑"
              }
            ]
            """;

    private final LingoLensLlmClient llmClient;

    public List<LensGroundingNodeDTO> recognize(String imageReference,
                                                 String sourceLanguage,
                                                 String targetLanguage) {
        String userPrompt = """
                Analyze this frozen camera frame now.
                Native language: %s
                Learning language: %s
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
