package com.hatsune.lingolens.module.memorypalace.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.lock.RedisDistributedLock;
import com.hatsune.lingolens.common.redis.RedisKeyConstant;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceListBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordAddBO;
import com.hatsune.lingolens.module.memorypalace.bo.MemoryPalaceWordPageBO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceItemDTO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceListRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceListResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordAddResponse;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordItemDTO;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordPageRequest;
import com.hatsune.lingolens.module.memorypalace.dto.MemoryPalaceWordPageResponse;
import com.hatsune.lingolens.module.memorypalace.manager.MemoryPalaceManager;
import com.hatsune.lingolens.module.memorypalace.service.MemoryPalaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
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

    private final MemoryPalaceManager memoryPalaceManager;
    private final RedisDistributedLock redisDistributedLock;
    private final CommonTools commonTools;

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
                    memoryPalaceManager.normalizeWordKey(request.getWord()));
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
            MemoryPalaceListBO listBO = memoryPalaceManager.list(UserContext.getUserId(), request);
            return buildListSuccessResponse(listBO);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("memory palace list failed, reason={}", e.getMessage());
            return buildListFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("memory palace list failed by system error", e);
            return buildListFailResponse("system busy, please try again later");
        }
    }

    @Override
    public MemoryPalaceWordPageResponse pageWords(@RequestBody String requestJson) {
        log.info("memory palace word page request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            MemoryPalaceWordPageRequest request = commonTools.parseJson(requestJson, MemoryPalaceWordPageRequest.class);
            validatePageWordsRequest(request);
            MemoryPalaceWordPageBO pageBO = memoryPalaceManager.pageWords(UserContext.getUserId(), request);
            return buildWordPageSuccessResponse(pageBO);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("memory palace word page failed, reason={}", e.getMessage());
            return buildWordPageFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("memory palace word page failed by system error", e);
            return buildWordPageFailResponse("system busy, please try again later");
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
        Assert.hasText(request.getWord(), "word must not be blank");
        Assert.isTrue(StringUtils.hasText(request.getTranslation()) || StringUtils.hasText(request.getDefinition()),
                "translation or definition must not be blank");
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

    private MemoryPalaceListResponse buildListSuccessResponse(MemoryPalaceListBO listBO) {
        return MemoryPalaceListResponse.builder()
                .result(ResponseResult.success())
                .pageNumber(listBO.getPageNumber())
                .pageSize(listBO.getPageSize())
                .total(listBO.getTotal())
                .palaces(toPalaceItems(listBO.getPalaces()))
                .build();
    }

    private MemoryPalaceListResponse buildListFailResponse(String message) {
        return MemoryPalaceListResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private MemoryPalaceWordPageResponse buildWordPageSuccessResponse(MemoryPalaceWordPageBO pageBO) {
        return MemoryPalaceWordPageResponse.builder()
                .result(ResponseResult.success())
                .pageNumber(pageBO.getPageNumber())
                .pageSize(pageBO.getPageSize())
                .total(pageBO.getTotal())
                .words(toWordItems(pageBO.getWords()))
                .build();
    }

    private MemoryPalaceWordPageResponse buildWordPageFailResponse(String message) {
        return MemoryPalaceWordPageResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private List<MemoryPalaceItemDTO> toPalaceItems(List<MemoryPalaceDO> palaces) {
        return palaces.stream()
                .map(this::toPalaceItem)
                .toList();
    }

    private MemoryPalaceItemDTO toPalaceItem(MemoryPalaceDO palace) {
        return MemoryPalaceItemDTO.builder()
                .palaceId(palace.getId())
                .sceneName(palace.getSceneName())
                .coverUrl(palace.getCoverUrl())
                .wordCount(palace.getWordCount())
                .updateTime(palace.getUpdateTime())
                .build();
    }

    private List<MemoryPalaceWordItemDTO> toWordItems(List<MemoryPalaceWordDO> words) {
        return words.stream()
                .map(this::toWordItem)
                .toList();
    }

    private MemoryPalaceWordItemDTO toWordItem(MemoryPalaceWordDO word) {
        return MemoryPalaceWordItemDTO.builder()
                .palaceWordId(word.getId())
                .palaceId(word.getPalaceId())
                .word(word.getWord())
                .displayWord(word.getDisplayWord())
                .phonetic(word.getPhonetic())
                .pronunciationUrl(word.getPronunciationUrl())
                .partOfSpeech(word.getPartOfSpeech())
                .translation(word.getTranslation())
                .definition(word.getDefinition())
                .exampleSentence(word.getExampleSentence())
                .exampleTranslation(word.getExampleTranslation())
                .sourceImageUrl(word.getSourceImageUrl())
                .createTime(word.getCreateTime())
                .build();
    }
}
