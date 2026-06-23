package com.hatsune.lingolens.module.lens.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.lock.RedisDistributedLock;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.infrastructure.llm.LingoLensLlmClient;
import com.hatsune.lingolens.module.lens.bo.LensRecognizeBO;
import com.hatsune.lingolens.module.lens.bo.LensRecognizedWordBO;
import com.hatsune.lingolens.module.lens.bo.LensWordInfoBO;
import com.hatsune.lingolens.module.lens.dto.LensBoxDTO;
import com.hatsune.lingolens.module.lens.dto.LensDetectedTagDTO;
import com.hatsune.lingolens.module.lens.dto.LensGroundingNodeDTO;
import com.hatsune.lingolens.module.lens.dto.LensGroundingResponse;
import com.hatsune.lingolens.module.lens.dto.LensRecognizeRequest;
import com.hatsune.lingolens.module.lens.dto.LensRecognizeResponse;
import com.hatsune.lingolens.module.lens.dto.LensRecognizedWordDTO;
import com.hatsune.lingolens.module.lens.dto.LensWordDetailRequest;
import com.hatsune.lingolens.module.lens.dto.LensWordDetailResponse;
import com.hatsune.lingolens.module.lens.dto.LensWordInfoDTO;
import com.hatsune.lingolens.module.lens.manager.LensGroundingManager;
import com.hatsune.lingolens.module.lens.manager.LensManager;
import com.hatsune.lingolens.module.lens.service.LensService;
import com.hatsune.lingolens.module.wordasset.manager.WordAssetWarmupManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class LensServiceImpl implements LensService {

    private static final String SOURCE_TYPE_DEMO = "demo";
    private static final String DEFAULT_SOURCE_LANGUAGE = "zh-CN";
    private static final String DEFAULT_TARGET_LANGUAGE = "en";
    private static final Duration WORD_ASSET_LOCK_TTL = Duration.ofSeconds(20);
    private static final String STREAM_SYSTEM_PROMPT = """
            Detect 5-8 clear physical objects.
            Output exactly:
            [word|translation|X|Y]
            One object per line.
            Rules:
            - word must be written in the requested source language.
            - translation must be written in the requested target language.
            - X and Y are integer percentages from 0 to 100 for the object center.
            - no markdown
            - no json
            - no explanations
            - raw text only
            """;

    private final LensManager lensManager;
    private final LensGroundingManager lensGroundingManager;
    private final LingoLensLlmClient llmClient;
    private final CommonTools commonTools;
    private final RedisDistributedLock redisDistributedLock;
    private final WordAssetWarmupManager wordAssetWarmupManager;

    @Override
    public LensRecognizeResponse recognize(@RequestBody String requestJson) {
        log.info("lens recognize request, requestJson={}", commonTools.maskSensitiveJson(requestJson, "imageBase64"));
        try {
            LensRecognizeRequest request = commonTools.parseJsonAllowBlank(requestJson, LensRecognizeRequest.class);
            commonTools.validateByJsr303(request);
            normalizeRecognizeRequest(request);
            validateRecognizeRequest(request);
            LensRecognizeBO recognizeBO = lensManager.recognize(UserContext.getUserId(), request);
            return buildRecognizeSuccessResponse(recognizeBO);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("lens recognize failed, reason={}", e.getMessage());
            return buildRecognizeFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("lens recognize failed by system error", e);
            return buildRecognizeFailResponse("system busy, please try again later");
        }
    }

    @Override
    public ResponseBodyEmitter recognizeStream(@RequestBody String requestJson) {
        log.info("lens recognize stream request, requestJson={}", commonTools.maskSensitiveJson(requestJson, "imageBase64"));
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(45_000L);
        try {
            LensRecognizeRequest request = commonTools.parseJsonAllowBlank(requestJson, LensRecognizeRequest.class);
            commonTools.validateByJsr303(request);
            normalizeRecognizeRequest(request);
            validateRecognizeRequest(request);
            CompletableFuture.runAsync(() -> streamCloudTags(request, emitter));
        } catch (BizException | IllegalArgumentException e) {
            completeEmitterWithLine(emitter, "# error: " + e.getMessage());
        } catch (Exception e) {
            log.error("lens recognize stream failed by system error", e);
            completeEmitterWithLine(emitter, "# error: system busy");
        }
        return emitter;
    }

    @Override
    public LensGroundingResponse recognizeGrounding(@RequestBody String requestJson) {
        log.info("lens grounding request, requestJson={}",
                commonTools.maskSensitiveJson(requestJson, "imageBase64"));
        try {
            LensRecognizeRequest request = commonTools.parseJsonAllowBlank(
                    requestJson, LensRecognizeRequest.class);
            commonTools.validateByJsr303(request);
            normalizeRecognizeRequest(request);
            Assert.isTrue(hasImagePayload(request), "image payload must not be blank");
            ImageDimensions dimensions = imageDimensions(request);
            List<LensGroundingNodeDTO> nodes = lensGroundingManager.recognize(
                    imageReference(request),
                    request.getSourceLanguage(),
                    request.getTargetLanguage(),
                    dimensions.width(),
                    dimensions.height());
            LensGroundingResponse response = LensGroundingResponse.builder()
                    .result(ResponseResult.success())
                    .nodes(nodes)
                    .build();
            log.info("lens grounding response built, nodeCount={}, warmupMode=fire_and_forget", nodes.size());
            scheduleGroundedWordAssetWarmup(nodes);
            return response;
        } catch (BizException | IllegalArgumentException e) {
            return LensGroundingResponse.builder()
                    .result(ResponseResult.fail(e.getMessage()))
                    .nodes(List.of())
                    .build();
        } catch (Exception e) {
            log.error("lens grounding failed", e);
            return LensGroundingResponse.builder()
                    .result(ResponseResult.fail("system busy, please try again later"))
                    .nodes(List.of())
                    .build();
        }
    }

    @Override
    public ResponseBodyEmitter recognizeGroundingStream(@RequestBody String requestJson) {
        log.info("lens grounding stream request, requestJson={}",
                commonTools.maskSensitiveJson(requestJson, "imageBase64"));
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(60_000L);
        try {
            LensRecognizeRequest request = commonTools.parseJsonAllowBlank(
                    requestJson, LensRecognizeRequest.class);
            commonTools.validateByJsr303(request);
            normalizeRecognizeRequest(request);
            Assert.isTrue(hasImagePayload(request), "image payload must not be blank");
            CompletableFuture.runAsync(() -> streamGroundingNodes(request, emitter));
        } catch (BizException | IllegalArgumentException e) {
            completeEmitterWithLine(emitter, "# error: " + e.getMessage());
        } catch (Exception e) {
            log.error("lens grounding stream failed by system error", e);
            completeEmitterWithLine(emitter, "# error: system busy");
        }
        return emitter;
    }

    @Override
    public LensWordDetailResponse wordDetail(@RequestBody String requestJson) {
        log.info("lens word detail request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            LensWordDetailRequest request = commonTools.parseJson(requestJson, LensWordDetailRequest.class);
            commonTools.validateByJsr303(request);
            normalizeWordDetailRequest(request);
            validateWordDetailRequest(request);
            LensWordInfoBO wordInfo = redisDistributedLock.execute(
                    RedisKeyConstant.WORD_ASSET_RESOLVE_LOCK,
                    WORD_ASSET_LOCK_TTL,
                    () -> lensManager.wordDetail(request),
                    request.getSourceLanguage(),
                    request.getTargetLanguage(),
                    request.getWord().toLowerCase(Locale.ROOT));
            return buildWordDetailSuccessResponse(wordInfo);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("lens word detail failed, reason={}", e.getMessage());
            return buildWordDetailFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("lens word detail failed by system error", e);
            return buildWordDetailFailResponse("system busy, please try again later");
        }
    }

    private void normalizeRecognizeRequest(LensRecognizeRequest request) {
        if (request.getImageUrl() != null) {
            request.setImageUrl(request.getImageUrl().trim());
        }
        if (request.getImageBase64() != null) {
            request.setImageBase64(request.getImageBase64().trim());
        }
        if (request.getImageContentType() != null) {
            request.setImageContentType(request.getImageContentType().trim().toLowerCase(Locale.ROOT));
        }
        if (request.getSceneName() != null) {
            request.setSceneName(request.getSceneName().trim());
        }
        if (StringUtils.hasText(request.getSourceType())) {
            request.setSourceType(request.getSourceType().trim().toLowerCase(Locale.ROOT));
        } else {
            request.setSourceType(SOURCE_TYPE_DEMO);
        }
        if (request.getClientTraceId() != null) {
            request.setClientTraceId(request.getClientTraceId().trim());
        }
        request.setSourceLanguage(normalizeLanguage(request.getSourceLanguage(), DEFAULT_SOURCE_LANGUAGE));
        request.setTargetLanguage(normalizeLanguage(request.getTargetLanguage(), DEFAULT_TARGET_LANGUAGE));
        if (request.getDetectedTags() != null) {
            request.getDetectedTags().forEach(this::normalizeDetectedTag);
        }
    }

    private void scheduleGroundedWordAssetWarmup(List<LensGroundingNodeDTO> nodes) {
        try {
            if (nodes == null || nodes.isEmpty()) {
                log.info("lens grounding word asset warmup skipped, reason=no_nodes");
                return;
            }
            List<String> wordKeys = nodes.stream()
                    .map(LensGroundingNodeDTO::getId)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .toList();
            if (wordKeys.isEmpty()) {
                log.info("lens grounding word asset warmup skipped, reason=no_word_keys");
                return;
            }
            log.info("lens grounding word asset warmup fire-and-forget submit, count={}, wordKeys={}",
                    wordKeys.size(), wordKeys);
            wordAssetWarmupManager.warmupCoreLanguagePairs(wordKeys);
        } catch (Exception e) {
            log.warn("lens grounding word asset warmup submit failed, response is not blocked", e);
        }
    }

    private void validateRecognizeRequest(LensRecognizeRequest request) {
        Assert.notNull(request, "lens recognize request must not be null");
        if (!SOURCE_TYPE_DEMO.equals(request.getSourceType()) && !hasDetectedTags(request)) {
            Assert.isTrue(hasImagePayload(request), "image payload must not be blank");
        }
    }

    private void normalizeDetectedTag(LensDetectedTagDTO tag) {
        if (tag == null) {
            return;
        }
        if (tag.getTag() != null) {
            tag.setTag(tag.getTag().trim());
        }
        if (tag.getDisplayWord() != null) {
            tag.setDisplayWord(tag.getDisplayWord().trim());
        }
    }

    private boolean hasDetectedTags(LensRecognizeRequest request) {
        return request.getDetectedTags() != null && !request.getDetectedTags().isEmpty();
    }

    private boolean hasImagePayload(LensRecognizeRequest request) {
        return StringUtils.hasText(request.getImageUrl()) || StringUtils.hasText(request.getImageBase64());
    }

    private void streamGroundingNodes(LensRecognizeRequest request, ResponseBodyEmitter emitter) {
        List<LensGroundingNodeDTO> emittedNodes = new ArrayList<>();
        try {
            ImageDimensions dimensions = imageDimensions(request);
            lensGroundingManager.recognizeStream(
                    imageReference(request),
                    request.getSourceLanguage(),
                    request.getTargetLanguage(),
                    dimensions.width(),
                    dimensions.height(),
                    node -> {
                        try {
                            emittedNodes.add(node);
                            emitter.send(com.alibaba.fastjson2.JSON.toJSONString(node) + "\n", MediaType.TEXT_PLAIN);
                        } catch (Exception sendError) {
                            throw new IllegalStateException(sendError);
                        }
                    });
            scheduleGroundedWordAssetWarmup(emittedNodes);
            emitter.complete();
        } catch (Exception e) {
            log.warn("lens grounding stream emit failed", e);
            emitter.completeWithError(e);
        }
    }

    private void streamCloudTags(LensRecognizeRequest request, ResponseBodyEmitter emitter) {
        try {
            List<String> unresolvedTags = request.getDetectedTags() == null
                    ? List.of()
                    : request.getDetectedTags().stream()
                    .map(LensDetectedTagDTO::getTag)
                    .filter(StringUtils::hasText)
                    .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                    .toList();
            String userPrompt = buildStreamUserPrompt(unresolvedTags,
                    request.getSourceLanguage(),
                    request.getTargetLanguage());
            llmClient.completeTextLineStream(STREAM_SYSTEM_PROMPT, userPrompt, imageReference(request), line -> {
                try {
                    emitter.send(line + "\n", MediaType.TEXT_PLAIN);
                } catch (Exception sendError) {
                    throw new IllegalStateException(sendError);
                }
            });
            emitter.complete();
        } catch (Exception e) {
            log.warn("lens recognize stream emit failed", e);
            emitter.completeWithError(e);
        }
    }

    private String imageReference(LensRecognizeRequest request) {
        if (StringUtils.hasText(request.getImageBase64())) {
            String contentType = StringUtils.hasText(request.getImageContentType())
                    ? request.getImageContentType()
                    : "image/jpeg";
            return "data:%s;base64,%s".formatted(contentType, request.getImageBase64());
        }
        return request.getImageUrl();
    }

    private ImageDimensions imageDimensions(LensRecognizeRequest request) {
        if (!StringUtils.hasText(request.getImageBase64())) {
            return ImageDimensions.UNKNOWN;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(request.getImageBase64());
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return ImageDimensions.UNKNOWN;
            }
            return new ImageDimensions(image.getWidth(), image.getHeight());
        } catch (Exception e) {
            log.debug("lens image dimension decode skipped", e);
            return ImageDimensions.UNKNOWN;
        }
    }

    private String buildStreamUserPrompt(List<String> unresolvedTags,
                                         String sourceLanguage,
                                         String targetLanguage) {
        List<String> promptLines = new ArrayList<>();
        promptLines.add("Analyze the uploaded image and return visible physical object tags.");
        promptLines.add("Use the required [word|translation|X|Y] format only.");
        promptLines.add("sourceLanguage=" + sourceLanguage + ", targetLanguage=" + targetLanguage + ".");
        promptLines.add("The first field word must be the object name in sourceLanguage.");
        promptLines.add("The second field translation must be the concise object name in targetLanguage.");
        if (unresolvedTags != null && !unresolvedTags.isEmpty()) {
            promptLines.add("Prioritize these YOLO objects that were not found in the local dictionary: "
                    + String.join(", ", unresolvedTags));
        }
        return String.join("\n", promptLines);
    }

    private void completeEmitterWithLine(ResponseBodyEmitter emitter, String line) {
        try {
            emitter.send(line + "\n", MediaType.TEXT_PLAIN);
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private record ImageDimensions(Integer width, Integer height) {
        private static final ImageDimensions UNKNOWN = new ImageDimensions(null, null);
    }

    private void normalizeWordDetailRequest(LensWordDetailRequest request) {
        if (request.getWord() != null) {
            request.setWord(request.getWord().trim());
        }
        if (request.getSceneName() != null) {
            request.setSceneName(request.getSceneName().trim());
        }
        request.setSourceLanguage(normalizeLanguage(request.getSourceLanguage(), DEFAULT_SOURCE_LANGUAGE));
        request.setTargetLanguage(normalizeLanguage(request.getTargetLanguage(), DEFAULT_TARGET_LANGUAGE));
    }

    private String normalizeLanguage(String language, String defaultValue) {
        if (!StringUtils.hasText(language)) {
            return defaultValue;
        }
        return language.trim();
    }

    private void validateWordDetailRequest(LensWordDetailRequest request) {
        Assert.notNull(request, "lens word detail request must not be null");
        Assert.hasText(request.getWord(), "word must not be blank");
    }

    private LensRecognizeResponse buildRecognizeSuccessResponse(LensRecognizeBO recognizeBO) {
        return LensRecognizeResponse.builder()
                .result(ResponseResult.success())
                .sceneId(recognizeBO.getSceneId())
                .sceneName(recognizeBO.getSceneName())
                .words(toRecognizedWordItems(recognizeBO.getWords()))
                .build();
    }

    private LensRecognizeResponse buildRecognizeFailResponse(String message) {
        return LensRecognizeResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private List<LensRecognizedWordDTO> toRecognizedWordItems(List<LensRecognizedWordBO> words) {
        return words.stream()
                .map(this::toRecognizedWordItem)
                .toList();
    }

    private LensRecognizedWordDTO toRecognizedWordItem(LensRecognizedWordBO word) {
        return LensRecognizedWordDTO.builder()
                .recognitionId(word.getRecognitionId())
                .wordAssetId(word.getWordAssetId())
                .word(word.getWord())
                .displayWord(word.getDisplayWord())
                .confidence(word.getConfidence())
                .box(LensBoxDTO.builder()
                        .x(word.getX())
                        .y(word.getY())
                        .width(word.getWidth())
                        .height(word.getHeight())
                        .build())
                .build();
    }

    private LensWordDetailResponse buildWordDetailSuccessResponse(LensWordInfoBO wordInfo) {
        return LensWordDetailResponse.builder()
                .result(ResponseResult.success())
                .wordInfo(toWordInfo(wordInfo))
                .build();
    }

    private LensWordDetailResponse buildWordDetailFailResponse(String message) {
        return LensWordDetailResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private LensWordInfoDTO toWordInfo(LensWordInfoBO wordInfo) {
        return LensWordInfoDTO.builder()
                .wordAssetId(wordInfo.getWordAssetId())
                .lang(wordInfo.getLang())
                .word(wordInfo.getWord())
                .displayWord(wordInfo.getDisplayWord())
                .phonetic(wordInfo.getPhonetic())
                .pronunciationBaseUrl(wordInfo.getPronunciationBaseUrl())
                .pronunciationPath(wordInfo.getPronunciationPath())
                .pronunciationUrl(wordInfo.getPronunciationUrl())
                .pos(wordInfo.getPos())
                .partOfSpeech(wordInfo.getPartOfSpeech())
                .explanation(wordInfo.getExplanation())
                .translation(wordInfo.getTranslation())
                .definition(wordInfo.getDefinition())
                .example(wordInfo.getExample())
                .exampleSentence(wordInfo.getExampleSentence())
                .exampleTranslation(wordInfo.getExampleTranslation())
                .assetImageUrl(wordInfo.getAssetImageUrl())
                .build();
    }
}
