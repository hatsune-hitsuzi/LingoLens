package com.hatsune.lingolens.module.lens.manager;

import com.hatsune.lingolens.module.lens.bo.LensRecognizeBO;
import com.hatsune.lingolens.module.lens.bo.LensRecognizedWordBO;
import com.hatsune.lingolens.module.lens.bo.LensWordInfoBO;
import com.hatsune.lingolens.module.lens.dto.LensDetectedTagDTO;
import com.hatsune.lingolens.module.lens.dto.LensRecognizeRequest;
import com.hatsune.lingolens.module.lens.dto.LensWordDetailRequest;
import com.hatsune.lingolens.module.wordasset.manager.WordAssetManager;
import com.hatsune.lingolens.module.wordasset.manager.WordAssetLocalizer;
import com.hatsune.lingolens.module.wordasset.manager.WordAssetWarmupManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LensManager {

    private static final String DEFAULT_SCENE_NAME = "\u6d45\u8349\u5496\u5561\u9986";
    private static final Map<String, LensWordInfoBO> WORD_INFO = Map.of(
            "mug", LensWordInfoBO.builder()
                    .word("mug")
                    .displayWord("Mug")
                    .phonetic("[m\u028cg]")
                    .pronunciationUrl("")
                    .partOfSpeech("n.")
                    .translation("\u9a6c\u514b\u676f")
                    .definition("a large cup with a handle")
                    .exampleSentence("She poured steaming coffee into her favorite ceramic mug.")
                    .exampleTranslation("\u5979\u628a\u70ed\u5496\u5561\u5012\u8fdb\u6700\u559c\u6b22\u7684\u9676\u74f7\u9a6c\u514b\u676f\u91cc\u3002")
                    .build(),
            "keyboard", LensWordInfoBO.builder()
                    .word("keyboard")
                    .displayWord("Keyboard")
                    .phonetic("[\u02c8ki\u02d0b\u0254\u02d0rd]")
                    .pronunciationUrl("")
                    .partOfSpeech("n.")
                    .translation("\u952e\u76d8")
                    .definition("a panel of keys used for typing on a computer")
                    .exampleSentence("The mechanical keyboard makes a satisfying click sound with every keystroke.")
                    .exampleTranslation("\u673a\u68b0\u952e\u76d8\u5728\u6bcf\u6b21\u6309\u952e\u65f6\u90fd\u4f1a\u53d1\u51fa\u4ee4\u4eba\u6109\u60a6\u7684\u54d2\u54d2\u58f0\u3002")
                    .build()
    );

    private final WordAssetManager wordAssetManager;
    private final WordAssetLocalizer wordAssetLocalizer;
    private final WordAssetWarmupManager wordAssetWarmupManager;

    public LensRecognizeBO recognize(Long userPkId, LensRecognizeRequest request) {
        String sceneName = StringUtils.hasText(request.getSceneName()) ? request.getSceneName() : DEFAULT_SCENE_NAME;
        Long sceneId = buildSceneId(userPkId, sceneName, request.getClientTraceId(), request.getImageUrl());
        List<LensRecognizedWordBO> words = buildRecognizedWords(userPkId, sceneId, request);
        wordAssetWarmupManager.warmupCoreLanguagePairs(words.stream()
                .map(LensRecognizedWordBO::getWord)
                .toList());
        return LensRecognizeBO.builder()
                .sceneId(sceneId)
                .sceneName(sceneName)
                .words(words)
                .build();
    }

    public LensWordInfoBO wordDetail(LensWordDetailRequest request) {
        return wordAssetManager.resolveWord(request);
    }

    private List<LensRecognizedWordBO> buildRecognizedWords(Long userPkId,
                                                            Long sceneId,
                                                            LensRecognizeRequest request) {
        if (hasDetectedTags(request)) {
            List<LensRecognizedWordBO> words = buildRecognizedWordsFromTags(userPkId, sceneId, request.getDetectedTags());
            if (!words.isEmpty()) {
                return words;
            }
            return buildRecognizedWordsFromImage(userPkId, sceneId, request);
        }
        return buildRecognizedWordsFromImage(userPkId, sceneId, request);
    }

    private List<LensRecognizedWordBO> buildDemoRecognizedWords(Long userPkId, Long sceneId) {
        List<LensRecognizedWordBO> words = new ArrayList<>();
        words.add(buildRecognizedWord(userPkId, sceneId, 1, "mug", 0.96D, 95, 320, 80, 40));
        words.add(buildRecognizedWord(userPkId, sceneId, 2, "keyboard", 0.94D, 230, 460, 112, 46));
        return words;
    }

    private List<LensRecognizedWordBO> buildRecognizedWordsFromTags(Long userPkId,
                                                                    Long sceneId,
                                                                    List<LensDetectedTagDTO> detectedTags) {
        List<LensRecognizedWordBO> words = new ArrayList<>();
        Set<String> usedWordKeys = new LinkedHashSet<>();
        int index = 1;
        for (LensDetectedTagDTO tag : detectedTags) {
            String wordKey = normalizeTag(tag);
            if (!StringUtils.hasText(wordKey) || !usedWordKeys.add(wordKey)) {
                continue;
            }
            LensRecognizedWordBO word = buildRecognizedWord(
                    userPkId,
                    sceneId,
                    index,
                    wordKey,
                    defaultConfidence(tag.getConfidence()),
                    defaultBoxValue(tag.getX(), 70 + (index % 2) * 150),
                    defaultBoxValue(tag.getY(), 270 + index * 58),
                    defaultBoxValue(tag.getWidth(), 104),
                    defaultBoxValue(tag.getHeight(), 44)
            );
            if (StringUtils.hasText(tag.getDisplayWord())
                    && (!StringUtils.hasText(word.getDisplayWord()) || word.getDisplayWord().equals(word.getWord()))) {
                word.setDisplayWord(tag.getDisplayWord().trim());
            }
            words.add(word);
            index++;
        }
        return words;
    }

    private List<LensRecognizedWordBO> buildRecognizedWordsFromImage(Long userPkId,
                                                                     Long sceneId,
                                                                     LensRecognizeRequest request) {
        if (!StringUtils.hasText(request.getImageUrl())) {
            return List.of();
        }
        try {
            LensWordInfoBO wordInfo = wordAssetLocalizer.localizeImageForUser(userPkId, request.getImageUrl());
            if (wordInfo == null || !StringUtils.hasText(wordInfo.getWord())) {
                return List.of();
            }
            return List.of(LensRecognizedWordBO.builder()
                    .recognitionId("lens_" + sceneId + "_image_1")
                    .wordAssetId(wordInfo.getWordAssetId())
                    .word(wordInfo.getWord())
                    .displayWord(wordInfo.getDisplayWord())
                    .confidence(0.98D)
                    .x(92)
                    .y(248)
                    .width(180)
                    .height(86)
                    .build());
        } catch (Exception e) {
            log.warn("lens image llm recognition skipped, imageUrl={}", request.getImageUrl(), e);
            return List.of();
        }
    }

    private LensRecognizedWordBO buildRecognizedWord(Long userPkId, Long sceneId, int index, String wordKey,
                                                     Double confidence, Integer x, Integer y,
                                                     Integer width, Integer height) {
        LensWordInfoBO wordInfo = resolveRecognizedWord(userPkId, wordKey);
        return LensRecognizedWordBO.builder()
                .recognitionId("lens_" + sceneId + "_" + index)
                .wordAssetId(wordInfo.getWordAssetId())
                .word(wordInfo.getWord())
                .displayWord(wordInfo.getDisplayWord())
                .confidence(confidence)
                .x(x)
                .y(y)
                .width(width)
                .height(height)
                .build();
    }

    private LensWordInfoBO resolveRecognizedWord(Long userPkId, String wordKey) {
        try {
            LensWordInfoBO fastWord = wordAssetLocalizer.localizeFastForUser(userPkId, wordKey);
            if (fastWord != null) {
                return fastWord;
            }
        } catch (Exception e) {
            log.warn("lens recognized word localization skipped, wordKey={}", wordKey, e);
        }
        try {
            LensWordInfoBO generatedWord = wordAssetLocalizer.localizeForUser(userPkId, wordKey);
            if (generatedWord != null) {
                return generatedWord;
            }
        } catch (Exception e) {
            log.warn("lens recognized word llm localization skipped, wordKey={}", wordKey, e);
        }
        LensWordInfoBO demoWord = WORD_INFO.get(wordKey);
        return demoWord == null ? fallbackRecognizedWord(wordKey) : demoWord;
    }

    private Long buildSceneId(Long userPkId, String sceneName, String clientTraceId, String imageUrl) {
        int hash = Objects.hash(userPkId, sceneName, clientTraceId, imageUrl);
        return 10_000L + Math.floorMod(hash, 900_000);
    }

    private boolean hasDetectedTags(LensRecognizeRequest request) {
        return request.getDetectedTags() != null && !request.getDetectedTags().isEmpty();
    }

    private String normalizeTag(LensDetectedTagDTO tag) {
        if (tag == null || !StringUtils.hasText(tag.getTag())) {
            return "";
        }
        return tag.getTag()
                .trim()
                .replace('_', ' ')
                .toLowerCase(Locale.ROOT);
    }

    private Double defaultConfidence(Double confidence) {
        return confidence == null ? 0.86D : confidence;
    }

    private Integer defaultBoxValue(Integer value, Integer defaultValue) {
        return value == null || value < 0 ? defaultValue : value;
    }

    private LensWordInfoBO fallbackRecognizedWord(String wordKey) {
        return LensWordInfoBO.builder()
                .word(wordKey)
                .displayWord(toDisplayWord(wordKey))
                .build();
    }

    private String toDisplayWord(String wordKey) {
        if (!StringUtils.hasText(wordKey)) {
            return "Object";
        }
        return wordKey.substring(0, 1).toUpperCase(Locale.ROOT) + wordKey.substring(1);
    }

}
