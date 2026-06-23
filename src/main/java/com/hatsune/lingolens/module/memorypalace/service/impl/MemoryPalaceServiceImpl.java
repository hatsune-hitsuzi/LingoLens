package com.hatsune.lingolens.module.memorypalace.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.lock.RedisDistributedLock;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceCreateBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceListBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordAddBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordPageBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordMutationBO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceCreateRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceCreateResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceDeleteRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceDeleteResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceItemDTO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceListRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceListResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceReorderRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceReorderResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceSearchRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceSearchResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceSearchWordDTO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceUpdateRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceUpdateResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordItemDTO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordPageRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordPageResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordDeleteRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordDeleteResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordMoveRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordMoveResponse;
import com.hatsune.lingolens.module.memorypalace.manager.MemoryPalaceManager;
import com.hatsune.lingolens.module.memorypalace.service.MemoryPalaceService;
import com.hatsune.lingolens.module.memorypalace.support.SceneNameLocalizer;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.manager.UserPreferenceManager;
import com.hatsune.lingolens.module.wordasset.support.PronunciationUrlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class MemoryPalaceServiceImpl implements MemoryPalaceService {

    private static final Duration ADD_WORD_LOCK_TTL = Duration.ofSeconds(10);
    private static final DateTimeFormatter PALACE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final MemoryPalaceManager memoryPalaceManager;
    private final UserPreferenceManager userPreferenceManager;
    private final SceneNameLocalizer sceneNameLocalizer;
    private final RedisDistributedLock redisDistributedLock;
    private final CommonTools commonTools;
    private final PronunciationUrlBuilder pronunciationUrlBuilder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoryPalaceWordAddResponse addWord(@RequestBody String requestJson) {
        log.info("memory palace word add request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceWordAddRequest request = commonTools.parseJson(requestJson, MemoryPalaceWordAddRequest.class);
            commonTools.validateByJsr303(request);
            normalizeAddRequest(request);
            validateAddRequest(request);
            Long userPkId = UserContext.getUserId();
            MemoryPalaceWordAddBO addResult = redisDistributedLock.execute(
                    RedisKeyConstant.MEMORY_PALACE_WORD_ADD_LOCK,
                    ADD_WORD_LOCK_TTL,
                    () -> memoryPalaceManager.addWord(userPkId, request),
                    userPkId,
                    memoryPalaceManager.normalizeSceneName(request.getSceneName()),
                    memoryPalaceManager.normalizeWordLockKey(request));
            return buildAddSuccessResponse(addResult);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("memory palace word add failed, reason={}", e.getMessage());
            return buildAddFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("memory palace word add failed by system error", e);
            return buildAddFailResponse("system busy, please try again later");
        }
    }

    @Override
    public MemoryPalaceListResponse list(@RequestBody String requestJson) {
        log.info("memory palace list request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceListRequest request = commonTools.parseJsonAllowBlank(requestJson, MemoryPalaceListRequest.class);
            Long userPkId = UserContext.getUserId();
            MemoryPalaceListBO listBO = memoryPalaceManager.list(userPkId, request);
            String displayLanguage = resolveDisplayLanguage(userPkId, request.getDisplayLanguage());
            return buildListSuccessResponse(userPkId, displayLanguage, listBO);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("memory palace list failed, reason={}", e.getMessage());
            return buildListFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("memory palace list failed by system error", e);
            return buildListFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoryPalaceCreateResponse create(@RequestBody String requestJson) {
        log.info("memory palace create request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceCreateRequest request = commonTools.parseJson(requestJson, MemoryPalaceCreateRequest.class);
            commonTools.validateByJsr303(request);
            Long userPkId = UserContext.getUserId();
            String displayLanguage = resolveDisplayLanguage(userPkId, request.getDisplayLanguage());
            MemoryPalaceCreateBO createBO = memoryPalaceManager.createPalace(
                    userPkId,
                    request.getSceneName(),
                    request.getCoverUrl());
            return buildCreateSuccessResponse(userPkId, displayLanguage, createBO);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("memory palace create failed, reason={}", e.getMessage());
            return buildCreateFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("memory palace create failed by system error", e);
            return buildCreateFailResponse("system busy, please try again later");
        }
    }

    @Override
    public MemoryPalaceSearchResponse search(@RequestBody String requestJson) {
        log.info("memory palace search request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceSearchRequest request = commonTools.parseJsonAllowBlank(requestJson, MemoryPalaceSearchRequest.class);
            commonTools.validateByJsr303(request);
            Long userPkId = UserContext.getUserId();
            String keyword = request.getKeyword() == null ? "" : request.getKeyword().trim();
            String displayLanguage = resolveDisplayLanguage(userPkId, request.getDisplayLanguage());
            int limit = normalizeSearchLimit(request.getLimit());
            List<MemoryPalaceDO> palaces = memoryPalaceManager.searchPalaces(userPkId, keyword, limit);
            List<MemoryPalaceSearchWordDTO> words = memoryPalaceManager.searchWords(userPkId, keyword, limit);
            return buildSearchSuccessResponse(userPkId, displayLanguage, keyword, palaces, words);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("memory palace search failed, reason={}", e.getMessage());
            return buildSearchFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("memory palace search failed by system error", e);
            return buildSearchFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoryPalaceReorderResponse reorder(@RequestBody String requestJson) {
        log.info("memory palace reorder request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceReorderRequest request = commonTools.parseJson(requestJson, MemoryPalaceReorderRequest.class);
            commonTools.validateByJsr303(request);
            Long userPkId = UserContext.getUserId();
            int acceptedCount = memoryPalaceManager.reorder(userPkId, request.getPalaceIds());
            return buildReorderSuccessResponse(acceptedCount);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("memory palace reorder failed, reason={}", e.getMessage());
            return buildReorderFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("memory palace reorder failed by system error", e);
            return buildReorderFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoryPalaceUpdateResponse update(@RequestBody String requestJson) {
        log.info("memory palace update request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceUpdateRequest request = commonTools.parseJson(requestJson, MemoryPalaceUpdateRequest.class);
            commonTools.validateByJsr303(request);
            Long userPkId = UserContext.getUserId();
            String displayLanguage = resolveDisplayLanguage(userPkId, request.getDisplayLanguage());
            MemoryPalaceDO palace = memoryPalaceManager.updatePalaceName(
                    userPkId,
                    request.getPalaceId(),
                    request.getSceneName());
            return buildUpdateSuccessResponse(userPkId, displayLanguage, palace);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("memory palace update failed, reason={}", e.getMessage());
            return buildUpdateFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("memory palace update failed by system error", e);
            return buildUpdateFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoryPalaceDeleteResponse delete(@RequestBody String requestJson) {
        log.info("memory palace delete request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceDeleteRequest request = commonTools.parseJson(requestJson, MemoryPalaceDeleteRequest.class);
            commonTools.validateByJsr303(request);
            Long userPkId = UserContext.getUserId();
            boolean deleted = memoryPalaceManager.deletePalace(userPkId, request.getPalaceId());
            return buildDeleteSuccessResponse(request.getPalaceId(), deleted);
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("memory palace delete failed, reason={}", e.getMessage());
            return buildDeleteFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("memory palace delete failed by system error", e);
            return buildDeleteFailResponse("system busy, please try again later");
        }
    }

    @Override
    public MemoryPalaceWordPageResponse pageWords(@RequestBody String requestJson) {
        log.info("memory palace word page request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceWordPageRequest request = commonTools.parseJson(requestJson, MemoryPalaceWordPageRequest.class);
            validatePageWordsRequest(request);
            Long userPkId = UserContext.getUserId();
            MemoryPalaceWordPageBO pageBO = memoryPalaceManager.pageWords(userPkId, request);
            return buildWordPageSuccessResponse(userPkId, pageBO);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("memory palace word page failed, reason={}", e.getMessage());
            return buildWordPageFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("memory palace word page failed by system error", e);
            return buildWordPageFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoryPalaceWordDeleteResponse deleteWord(@RequestBody String requestJson) {
        log.info("memory palace word delete request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceWordDeleteRequest request = commonTools.parseJson(
                    requestJson, MemoryPalaceWordDeleteRequest.class);
            commonTools.validateByJsr303(request);
            MemoryPalaceWordMutationBO result = memoryPalaceManager.deleteWord(
                    UserContext.getUserId(), request.getPalaceWordId());
            return MemoryPalaceWordDeleteResponse.builder()
                    .result(ResponseResult.success("deleted"))
                    .palaceWordId(result.getPalaceWordId())
                    .palaceId(result.getSourcePalaceId())
                    .palaceWordCount(result.getSourcePalaceWordCount())
                    .deleted(result.getChanged())
                    .build();
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            return MemoryPalaceWordDeleteResponse.builder()
                    .result(ResponseResult.fail(e.getMessage()))
                    .deleted(false)
                    .build();
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("memory palace word delete failed", e);
            return MemoryPalaceWordDeleteResponse.builder()
                    .result(ResponseResult.fail("system busy, please try again later"))
                    .deleted(false)
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemoryPalaceWordMoveResponse moveWord(@RequestBody String requestJson) {
        log.info("memory palace word move request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceWordMoveRequest request = commonTools.parseJson(
                    requestJson, MemoryPalaceWordMoveRequest.class);
            commonTools.validateByJsr303(request);
            MemoryPalaceWordMutationBO result = memoryPalaceManager.moveWord(
                    UserContext.getUserId(),
                    request.getPalaceWordId(),
                    request.getTargetPalaceId());
            return MemoryPalaceWordMoveResponse.builder()
                    .result(ResponseResult.success(Boolean.TRUE.equals(result.getChanged()) ? "moved" : "unchanged"))
                    .palaceWordId(result.getPalaceWordId())
                    .sourcePalaceId(result.getSourcePalaceId())
                    .targetPalaceId(result.getTargetPalaceId())
                    .sourcePalaceWordCount(result.getSourcePalaceWordCount())
                    .targetPalaceWordCount(result.getTargetPalaceWordCount())
                    .moved(result.getChanged())
                    .build();
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            return MemoryPalaceWordMoveResponse.builder()
                    .result(ResponseResult.fail(e.getMessage()))
                    .moved(false)
                    .build();
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("memory palace word move failed", e);
            return MemoryPalaceWordMoveResponse.builder()
                    .result(ResponseResult.fail("system busy, please try again later"))
                    .moved(false)
                    .build();
        }
    }

    private void normalizeAddRequest(MemoryPalaceWordAddRequest request) {
        if (request.getSceneName() != null) {
            request.setSceneName(request.getSceneName().trim());
        }
        if (request.getWord() != null) {
            request.setWord(request.getWord().trim());
        }
        if (request.getDisplayWord() != null) {
            request.setDisplayWord(request.getDisplayWord().trim());
        }
        if (request.getTranslation() != null) {
            request.setTranslation(request.getTranslation().trim());
        }
    }

    private void validateAddRequest(MemoryPalaceWordAddRequest request) {
        Assert.notNull(request, "memory palace word add request must not be null");
        Assert.isTrue(request.getWordAssetId() != null || StringUtils.hasText(request.getWord()),
                "word asset id or word must not be blank");
    }

    private void validatePageWordsRequest(MemoryPalaceWordPageRequest request) {
        Assert.notNull(request, "memory palace word page request must not be null");
        Assert.notNull(request.getPalaceId(), "palace id must not be null");
    }

    private MemoryPalaceWordAddResponse buildAddSuccessResponse(MemoryPalaceWordAddBO addResult) {
        return MemoryPalaceWordAddResponse.builder()
                .result(ResponseResult.success(Boolean.TRUE.equals(addResult.getAdded()) ? "added" : "already added"))
                .palaceId(addResult.getPalaceId())
                .palaceWordId(addResult.getPalaceWordId())
                .palaceWordCount(addResult.getPalaceWordCount())
                .added(addResult.getAdded())
                .build();
    }

    private MemoryPalaceWordAddResponse buildAddFailResponse(String message) {
        return MemoryPalaceWordAddResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceListResponse buildListSuccessResponse(Long userPkId,
                                                              String displayLanguage,
                                                              MemoryPalaceListBO listBO) {
        return MemoryPalaceListResponse.builder()
                .result(ResponseResult.success())
                .pageNumber(listBO.getPageNumber())
                .pageSize(listBO.getPageSize())
                .total(listBO.getTotal())
                .palaces(toPalaceItems(userPkId, displayLanguage, listBO.getPalaces()))
                .build();
    }

    private MemoryPalaceListResponse buildListFailResponse(String message) {
        return MemoryPalaceListResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceCreateResponse buildCreateSuccessResponse(Long userPkId,
                                                                  String displayLanguage,
                                                                  MemoryPalaceCreateBO createBO) {
        return MemoryPalaceCreateResponse.builder()
                .result(ResponseResult.success(Boolean.TRUE.equals(createBO.getCreated()) ? "created" : "already exists"))
                .created(createBO.getCreated())
                .palace(toPalaceItem(userPkId, displayLanguage, createBO.getPalace()))
                .build();
    }

    private MemoryPalaceCreateResponse buildCreateFailResponse(String message) {
        return MemoryPalaceCreateResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceSearchResponse buildSearchSuccessResponse(Long userPkId,
                                                                  String displayLanguage,
                                                                  String keyword,
                                                                  List<MemoryPalaceDO> palaces,
                                                                  List<MemoryPalaceSearchWordDTO> words) {
        return MemoryPalaceSearchResponse.builder()
                .result(ResponseResult.success())
                .keyword(keyword)
                .palaces(toPalaceItems(userPkId, displayLanguage, palaces))
                .words(toSearchWordItems(displayLanguage, words))
                .build();
    }

    private MemoryPalaceSearchResponse buildSearchFailResponse(String message) {
        return MemoryPalaceSearchResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceReorderResponse buildReorderSuccessResponse(int acceptedCount) {
        return MemoryPalaceReorderResponse.builder()
                .result(ResponseResult.success())
                .acceptedCount(acceptedCount)
                .build();
    }

    private MemoryPalaceReorderResponse buildReorderFailResponse(String message) {
        return MemoryPalaceReorderResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceUpdateResponse buildUpdateSuccessResponse(Long userPkId,
                                                                  String displayLanguage,
                                                                  MemoryPalaceDO palace) {
        return MemoryPalaceUpdateResponse.builder()
                .result(ResponseResult.success())
                .palace(toPalaceItem(userPkId, displayLanguage, palace))
                .build();
    }

    private MemoryPalaceUpdateResponse buildUpdateFailResponse(String message) {
        return MemoryPalaceUpdateResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceDeleteResponse buildDeleteSuccessResponse(Long palaceId, boolean deleted) {
        return MemoryPalaceDeleteResponse.builder()
                .result(ResponseResult.success(Boolean.TRUE.equals(deleted) ? "deleted" : "not found"))
                .palaceId(palaceId)
                .deleted(deleted)
                .build();
    }

    private MemoryPalaceDeleteResponse buildDeleteFailResponse(String message) {
        return MemoryPalaceDeleteResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceWordPageResponse buildWordPageSuccessResponse(Long userPkId, MemoryPalaceWordPageBO pageBO) {
        return MemoryPalaceWordPageResponse.builder()
                .result(ResponseResult.success())
                .pageNumber(pageBO.getPageNumber())
                .pageSize(pageBO.getPageSize())
                .total(pageBO.getTotal())
                .words(toWordItems(userPkId, pageBO.getWords()))
                .build();
    }

    private MemoryPalaceWordPageResponse buildWordPageFailResponse(String message) {
        return MemoryPalaceWordPageResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private List<MemoryPalaceItemDTO> toPalaceItems(Long userPkId,
                                                    String displayLanguage,
                                                    List<MemoryPalaceDO> palaces) {
        return palaces.stream()
                .map(palace -> toPalaceItem(userPkId, displayLanguage, palace))
                .toList();
    }

    private MemoryPalaceItemDTO toPalaceItem(Long userPkId, String displayLanguage, MemoryPalaceDO palace) {
        return MemoryPalaceItemDTO.builder()
                .palaceId(palace.getId())
                .sceneName(palace.getSceneName())
                .localizedSceneName(sceneNameLocalizer.localize(palace.getSceneName(), displayLanguage))
                .coverUrl(palace.getCoverUrl())
                .savedPhotos(memoryPalaceManager.latestSavedPhotos(userPkId, palace.getId(), 3))
                .wordCount(palace.getWordCount())
                .updateTime(formatPalaceDate(palace.getUpdateTime()))
                .build();
    }

    private String resolveDisplayLanguage(Long userPkId, String requestedLanguage) {
        if (StringUtils.hasText(requestedLanguage)) {
            return requestedLanguage.trim();
        }
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        if (preference == null) {
            return null;
        }
        return StringUtils.hasText(preference.getUiLanguage())
                ? preference.getUiLanguage()
                : preference.getSourceLanguage();
    }

    private int normalizeSearchLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 20;
        }
        return Math.min(limit, 50);
    }

    private List<MemoryPalaceSearchWordDTO> toSearchWordItems(String displayLanguage,
                                                              List<MemoryPalaceSearchWordDTO> words) {
        return words.stream()
                .peek(word -> word.setLocalizedSceneName(
                        sceneNameLocalizer.localize(word.getSceneName(), displayLanguage)))
                .toList();
    }

    private List<MemoryPalaceWordItemDTO> toWordItems(Long userPkId, List<MemoryPalaceWordDO> words) {
        return words.stream()
                .map(this::toWordItem)
                .toList();
    }

    private MemoryPalaceWordItemDTO toWordItem(MemoryPalaceWordDO word) {
        return MemoryPalaceWordItemDTO.builder()
                .palaceWordId(word.getId())
                .palaceId(word.getPalaceId())
                .wordAssetId(word.getWordAssetId())
                .anchorXPercent(word.getAnchorXPercent())
                .anchorYPercent(word.getAnchorYPercent())
                .word(word.getWord())
                .displayWord(word.getDisplayWord())
                .phonetic(word.getPhonetic())
                .pronunciationUrl(pronunciationUrlBuilder.buildFromPath(word.getPronunciationUrl()))
                .partOfSpeech(word.getPartOfSpeech())
                .translation(word.getTranslation())
                .definition(word.getDefinition())
                .exampleSentence(word.getExampleSentence())
                .exampleTranslation(word.getExampleTranslation())
                .sourceImageUrl(word.getSourceImageUrl())
                .createTime(formatPalaceDate(word.getCreateTime()))
                .build();
    }

    private String formatPalaceDate(OffsetDateTime value) {
        return value == null ? "" : value.format(PALACE_DATE_FORMATTER);
    }
}
