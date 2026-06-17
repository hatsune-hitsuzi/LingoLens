package com.hatsune.lingolens.module.arscan.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.arscan.domain.ArScanWordDO;
import com.hatsune.lingolens.module.arscan.dto.ArScanWordItemDTO;
import com.hatsune.lingolens.module.arscan.dto.ArScanWordPageRequest;
import com.hatsune.lingolens.module.arscan.dto.ArScanWordPageResponse;
import com.hatsune.lingolens.module.arscan.manager.ArScanWordManager;
import com.hatsune.lingolens.module.arscan.service.ArScanWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ArScanWordServiceImpl implements ArScanWordService {

    private final ArScanWordManager arScanWordManager;
    private final CommonTools commonTools;

    @Override
    public ArScanWordPageResponse pageWords(@RequestBody(required = false) String requestJson) {
        log.info("ar scan word page request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            ArScanWordPageRequest request = commonTools.parseJsonAllowBlank(requestJson, ArScanWordPageRequest.class);
            int pageNumber = normalizePageNumber(request.getPageNumber());
            int pageSize = normalizePageSize(request.getPageSize());
            Long userPkId = UserContext.getUserId();
            List<ArScanWordDO> words = arScanWordManager.pageWords(
                    userPkId,
                    request.getKeyword(),
                    pageNumber,
                    pageSize);
            int total = arScanWordManager.countWords(userPkId, request.getKeyword());
            return ArScanWordPageResponse.builder()
                    .result(ResponseResult.success())
                    .pageNumber(pageNumber)
                    .pageSize(pageSize)
                    .total(total)
                    .words(toWordItems(userPkId, words))
                    .build();
        } catch (BizException | IllegalArgumentException e) {
            log.warn("ar scan word page failed, reason={}", e.getMessage());
            return ArScanWordPageResponse.builder()
                    .result(ResponseResult.fail(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("ar scan word page failed by system error", e);
            return ArScanWordPageResponse.builder()
                    .result(ResponseResult.fail("system busy, please try again later"))
                    .build();
        }
    }

    private List<ArScanWordItemDTO> toWordItems(Long userPkId, List<ArScanWordDO> words) {
        return words.stream().map(this::toWordItem).toList();
    }

    private ArScanWordItemDTO toWordItem(ArScanWordDO word) {
        return ArScanWordItemDTO.builder()
                .scanWordId(word.getId())
                .wordAssetId(word.getWordAssetId())
                .firstPalaceId(word.getFirstPalaceId())
                .sceneName(word.getSceneName())
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
                .updateTime(word.getUpdateTime())
                .build();
    }

    private int normalizePageNumber(Integer pageNumber) {
        return pageNumber == null || pageNumber < 1 ? 1 : pageNumber;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}
