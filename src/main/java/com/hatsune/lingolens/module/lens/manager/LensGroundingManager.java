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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class LensGroundingManager {

    private static final int MAX_NODES = 6;

    private static final String STREAM_SYSTEM_PROMPT = """
            You are Gemma-3n-e2b-it working as a fast structured multimodal grounding gateway.

            CRITICAL GOAL:
            Identify AT MOST 6 prominent, clear, distinct physical objects in the image.
            Stream results immediately. Output one complete JSON object per line as soon as each object is ready.

            Output schema for EACH LINE:
            {"polygon":[[10,20],[35,18],[38,46],[12,48]],"labels":{"en":"figurine","zh":"手办","ja":"フィギュア"},"phonetics":{"en":"/ˈfɪɡjərɪn/","zh":"shǒu bàn","ja":"figyua"}}

            Coordinate rules:
            - polygon MUST use normalized percentage coordinates from 0 to 100.
            - Do NOT output pixel coordinates such as 180, 560, or 720.
            - The frontend will render directly as left:X%, top:Y%.

            Label rules:
            - Do NOT output id. labels.en is the only stable English identifier.
            - labels.en must be lowercase English object words.
            - labels.zh must be real Simplified Chinese.
            - labels.ja must be real Japanese.
            - phonetics.en uses IPA when known, phonetics.zh uses pinyin, phonetics.ja uses romaji.
            - If phonetic is uncertain, use an empty string.

            Format rules:
            - One JSON object per line.
            - No wrapper object. No JSON array.
            - No prose, explanations, markdown, comments, or chain-of-thought.
            - Do not output commas between lines.
            """;

    private static final String BATCH_SYSTEM_PROMPT = """
            You are Gemma-3n-e2b-it working as a strict structured multimodal grounding gateway.
            Identify AT MOST 6 prominent, clear, distinct physical objects in the image.

            Output raw JSON object only:
            {
              "nodes": [
                {"polygon":[[10,20],[35,18],[38,46],[12,48]],"labels":{"en":"figurine","zh":"手办","ja":"フィギュア"},"phonetics":{"en":"/ˈfɪɡjərɪn/","zh":"shǒu bàn","ja":"figyua"}}
              ]
            }

            Rules:
            - polygon MUST use normalized percentage coordinates from 0 to 100.
            - Do NOT output id.
            - labels.en is the lowercase English identifier.
            - The first character must be "{".
            - No prose, explanations, markdown, comments, or chain-of-thought.
            """;

    private final LingoLensLlmClient llmClient;

    public List<LensGroundingNodeDTO> recognize(String imageReference,
                                                String sourceLanguage,
                                                String targetLanguage,
                                                Integer imageWidth,
                                                Integer imageHeight) {
        log.info("lens grounding llm recognize start, sourceLanguage={}, targetLanguage={}, imagePresent={}",
                sourceLanguage, targetLanguage, StringUtils.hasText(imageReference));
        String userPrompt = """
                Analyze this frozen camera frame now.
                Return at most 6 prominent physical objects with percentage polygon, English/Chinese/Japanese labels, and compact phonetics.

                REMINDER: Follow the System instructions.
                NO prose. NO explanations. NO Markdown blocks.
                The first output character must be '{'.
                """;
        JSONArray generated = llmClient.completeJsonArray(
                BATCH_SYSTEM_PROMPT,
                userPrompt,
                imageReference);
        Map<String, LensGroundingNodeDTO> deduplicated = new LinkedHashMap<>();
        for (int index = 0; index < generated.size() && deduplicated.size() < MAX_NODES; index++) {
            JSONObject item = generated.getJSONObject(index);
            LensGroundingNodeDTO node = normalizeNode(item, sourceLanguage, targetLanguage, imageWidth, imageHeight);
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

    public void recognizeStream(String imageReference,
                                String sourceLanguage,
                                String targetLanguage,
                                Integer imageWidth,
                                Integer imageHeight,
                                Consumer<LensGroundingNodeDTO> onNode) {
        log.info("lens grounding llm stream recognize start, sourceLanguage={}, targetLanguage={}, imagePresent={}",
                sourceLanguage, targetLanguage, StringUtils.hasText(imageReference));
        String userPrompt = """
                Analyze this frozen camera frame now.
                Output each object immediately as ONE JSON object per line.
                Coordinates must be percentage values from 0 to 100, never pixels.

                REMINDER:
                No id field. No prose. No markdown. No JSON array.
                First output line must start with "{".
                """;
        Set<String> emitted = new HashSet<>();
        llmClient.completeGroundingJsonLineStream(STREAM_SYSTEM_PROMPT, userPrompt, imageReference, line -> {
            if (emitted.size() >= MAX_NODES) {
                return;
            }
            JSONObject item = parseLineObject(line);
            LensGroundingNodeDTO node = normalizeNode(item, sourceLanguage, targetLanguage, imageWidth, imageHeight);
            if (node == null || emitted.contains(node.getId())) {
                return;
            }
            emitted.add(node.getId());
            onNode.accept(node);
        });
        log.info("lens grounding llm stream recognize finished, emittedCount={}, ids={}", emitted.size(), emitted);
    }

    private JSONObject parseLineObject(String line) {
        if (!StringUtils.hasText(line)) {
            return null;
        }
        String text = line.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        text = text.substring(start, end + 1);
        try {
            return JSONObject.parseObject(text);
        } catch (Exception e) {
            log.debug("lens grounding stream line parse skipped, line={}", line);
            return null;
        }
    }

    private LensGroundingNodeDTO normalizeNode(JSONObject item,
                                              String sourceLanguage,
                                              String targetLanguage,
                                              Integer imageWidth,
                                              Integer imageHeight) {
        if (item == null) {
            return null;
        }
        Map<String, String> labels = normalizeStringMap(item.getJSONObject("labels"));
        Map<String, String> phonetics = normalizeStringMap(item.getJSONObject("phonetics"));
        String id = normalizeId(defaultText(item.getString("id"), labels.get("en")));
        List<List<Double>> polygon = normalizePolygon(item.getJSONArray("polygon"), imageWidth, imageHeight);
        if (!StringUtils.hasText(id) || polygon.size() < 3) {
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

    private List<List<Double>> normalizePolygon(JSONArray polygon,
                                                Integer imageWidth,
                                                Integer imageHeight) {
        if (polygon == null) {
            return List.of();
        }
        List<double[]> rawPoints = new ArrayList<>();
        double maxX = 0D;
        double maxY = 0D;
        for (int index = 0; index < polygon.size() && rawPoints.size() < 80; index++) {
            JSONArray point = polygon.getJSONArray(index);
            if (point == null || point.size() < 2) {
                continue;
            }
            Double x = point.getDouble(0);
            Double y = point.getDouble(1);
            if (x == null || y == null || !Double.isFinite(x) || !Double.isFinite(y)) {
                continue;
            }
            rawPoints.add(new double[]{x, y});
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
        boolean normalizedUnit = maxX <= 1.5D && maxY <= 1.5D;
        boolean absolutePixels = maxX > 100D || maxY > 100D;
        double width = safeDimension(imageWidth, maxX);
        double height = safeDimension(imageHeight, maxY);
        List<List<Double>> points = new ArrayList<>();
        for (double[] raw : rawPoints) {
            double x = raw[0];
            double y = raw[1];
            if (normalizedUnit) {
                x *= 100D;
                y *= 100D;
            } else if (absolutePixels) {
                x = x / width * 100D;
                y = y / height * 100D;
            }
            points.add(List.of(clampPercent(x), clampPercent(y)));
        }
        return points;
    }

    private double safeDimension(Integer dimension, double fallback) {
        if (dimension != null && dimension > 0) {
            return dimension;
        }
        return fallback > 0 ? fallback : 100D;
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
