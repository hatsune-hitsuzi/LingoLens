package com.hatsune.lingolens.module.wordbook.service.impl;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.wordbook.bo.WordBookDetailBO;
import com.hatsune.lingolens.module.wordbook.bo.WordBookListBO;
import com.hatsune.lingolens.module.wordbook.bo.WordBookWordPageBO;
import com.hatsune.lingolens.module.wordbook.domain.WordBookDO;
import com.hatsune.lingolens.module.wordbook.domain.WordBookWordDO;
import com.hatsune.lingolens.module.wordbook.dto.WordBookDetailRequest;
import com.hatsune.lingolens.module.wordbook.dto.WordBookDetailResponse;
import com.hatsune.lingolens.module.wordbook.dto.WordBookItemDTO;
import com.hatsune.lingolens.module.wordbook.dto.WordBookListRequest;
import com.hatsune.lingolens.module.wordbook.dto.WordBookListResponse;
import com.hatsune.lingolens.module.wordbook.dto.WordBookWordItemDTO;
import com.hatsune.lingolens.module.wordbook.dto.WordBookWordPageRequest;
import com.hatsune.lingolens.module.wordbook.dto.WordBookWordPageResponse;
import com.hatsune.lingolens.module.wordbook.manager.WordBookManager;
import com.hatsune.lingolens.module.wordbook.service.WordBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class WordBookServiceImpl implements WordBookService {

    private final WordBookManager wordBookManager;
    private final CommonTools commonTools;

    @Override
    public WordBookListResponse list(@RequestBody String requestJson) {
        log.info("word book list request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            WordBookListRequest request = commonTools.parseJsonAllowBlank(requestJson, WordBookListRequest.class);
            commonTools.validateByJsr303(request);
            WordBookListBO wordBookList = wordBookManager.listWordBooks(request);
            return buildListSuccessResponse(wordBookList);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("word book list failed, reason={}", e.getMessage());
            return buildListFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("word book list failed by system error", e);
            return buildListFailResponse("system busy, please try again later");
        }
    }

    @Override
    public WordBookDetailResponse detail(@RequestBody String requestJson) {
        log.info("word book detail request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            WordBookDetailRequest request = commonTools.parseJson(requestJson, WordBookDetailRequest.class);
            commonTools.validateByJsr303(request);
            Assert.notNull(request.getBookId(), "book id must not be null");
            WordBookDetailBO wordBookDetail = wordBookManager.detail(request);
            return buildDetailSuccessResponse(wordBookDetail);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("word book detail failed, reason={}", e.getMessage());
            return buildDetailFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("word book detail failed by system error", e);
            return buildDetailFailResponse("system busy, please try again later");
        }
    }

    @Override
    public WordBookWordPageResponse pageWords(@RequestBody String requestJson) {
        log.info("word book word page request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            WordBookWordPageRequest request = commonTools.parseJson(requestJson, WordBookWordPageRequest.class);
            commonTools.validateByJsr303(request);
            Assert.notNull(request.getBookId(), "book id must not be null");
            WordBookWordPageBO wordPage = wordBookManager.pageWords(request);
            return buildWordPageSuccessResponse(wordPage);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("word book word page failed, reason={}", e.getMessage());
            return buildWordPageFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("word book word page failed by system error", e);
            return buildWordPageFailResponse("system busy, please try again later");
        }
    }

    private WordBookListResponse buildListSuccessResponse(WordBookListBO wordBookList) {
        return WordBookListResponse.builder()
                .result(ResponseResult.success())
                .pageNumber(wordBookList.getPageNumber())
                .pageSize(wordBookList.getPageSize())
                .total(wordBookList.getTotal())
                .wordBooks(toWordBookItems(wordBookList.getWordBooks()))
                .build();
    }

    private WordBookListResponse buildListFailResponse(String message) {
        return WordBookListResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private WordBookDetailResponse buildDetailSuccessResponse(WordBookDetailBO wordBookDetail) {
        return WordBookDetailResponse.builder()
                .result(ResponseResult.success())
                .wordBook(toWordBookItem(wordBookDetail.getWordBook()))
                .build();
    }

    private WordBookDetailResponse buildDetailFailResponse(String message) {
        return WordBookDetailResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private WordBookWordPageResponse buildWordPageSuccessResponse(WordBookWordPageBO wordPage) {
        return WordBookWordPageResponse.builder()
                .result(ResponseResult.success())
                .bookId(wordPage.getBookId())
                .pageNumber(wordPage.getPageNumber())
                .pageSize(wordPage.getPageSize())
                .total(wordPage.getTotal())
                .words(toWordItems(wordPage.getWords()))
                .build();
    }

    private WordBookWordPageResponse buildWordPageFailResponse(String message) {
        return WordBookWordPageResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private List<WordBookItemDTO> toWordBookItems(List<WordBookDO> wordBooks) {
        return wordBooks.stream()
                .map(this::toWordBookItem)
                .toList();
    }

    private WordBookItemDTO toWordBookItem(WordBookDO wordBook) {
        return WordBookItemDTO.builder()
                .bookId(wordBook.getId())
                .bookName(wordBook.getBookName())
                .description(wordBook.getDescription())
                .coverUrl(wordBook.getCoverUrl())
                .wordCount(wordBook.getWordCount())
                .difficultyLevel(wordBook.getDifficultyLevel())
                .status(wordBook.getStatus())
                .build();
    }

    private List<WordBookWordItemDTO> toWordItems(List<WordBookWordDO> words) {
        return words.stream()
                .map(this::toWordItem)
                .toList();
    }

    private WordBookWordItemDTO toWordItem(WordBookWordDO word) {
        return WordBookWordItemDTO.builder()
                .wordId(word.getId())
                .bookId(word.getBookId())
                .word(word.getWord())
                .phonetic(word.getPhonetic())
                .pronunciationUrl(word.getPronunciationUrl())
                .translation(word.getTranslation())
                .definition(word.getDefinition())
                .exampleSentence(word.getExampleSentence())
                .exampleTranslation(word.getExampleTranslation())
                .sortNo(word.getSortNo())
                .build();
    }
}
