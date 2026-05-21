package com.hatsune.lingolens.module.wordbook.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.wordbook.bo.WordBookDetailBO;
import com.hatsune.lingolens.module.wordbook.bo.WordBookListBO;
import com.hatsune.lingolens.module.wordbook.bo.WordBookWordPageBO;
import com.hatsune.lingolens.module.wordbook.domain.WordBookDO;
import com.hatsune.lingolens.module.wordbook.domain.WordBookWordDO;
import com.hatsune.lingolens.module.wordbook.dto.WordBookDetailRequest;
import com.hatsune.lingolens.module.wordbook.dto.WordBookListRequest;
import com.hatsune.lingolens.module.wordbook.dto.WordBookWordPageRequest;
import com.hatsune.lingolens.module.wordbook.mapper.WordBookMapper;
import com.hatsune.lingolens.module.wordbook.mapper.WordBookWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Component
@RequiredArgsConstructor
public class WordBookManager {

    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_BOOK_PAGE_SIZE = 20;
    private static final int DEFAULT_WORD_PAGE_SIZE = 50;

    private final WordBookMapper wordBookMapper;
    private final WordBookWordMapper wordBookWordMapper;

    public WordBookListBO listWordBooks(WordBookListRequest request) {
        int pageNumber = normalizePageNumber(request.getPageNumber());
        int pageSize = normalizePageSize(request.getPageSize(), DEFAULT_BOOK_PAGE_SIZE);
        String keyword = normalizeKeyword(request.getKeyword());
        Integer total = wordBookMapper.countWordBooks(keyword, request.getDifficultyLevel(), request.getStatus());
        List<WordBookDO> wordBooks = total == null || total == 0
                ? Collections.emptyList()
                : wordBookMapper.listWordBooks(keyword, request.getDifficultyLevel(), request.getStatus(), pageSize, offset(pageNumber, pageSize));
        return WordBookListBO.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .total(total == null ? 0 : total)
                .wordBooks(wordBooks)
                .build();
    }

    public WordBookDetailBO detail(WordBookDetailRequest request) {
        WordBookDO wordBook = findWordBook(request.getBookId());
        return WordBookDetailBO.builder()
                .wordBook(wordBook)
                .build();
    }

    public WordBookWordPageBO pageWords(WordBookWordPageRequest request) {
        findWordBook(request.getBookId());
        int pageNumber = normalizePageNumber(request.getPageNumber());
        int pageSize = normalizePageSize(request.getPageSize(), DEFAULT_WORD_PAGE_SIZE);
        String keyword = normalizeKeyword(request.getKeyword());
        Integer total = wordBookWordMapper.countWords(request.getBookId(), keyword);
        List<WordBookWordDO> words = total == null || total == 0
                ? Collections.emptyList()
                : wordBookWordMapper.listWords(request.getBookId(), keyword, pageSize, offset(pageNumber, pageSize));
        return WordBookWordPageBO.builder()
                .bookId(request.getBookId())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .total(total == null ? 0 : total)
                .words(words)
                .build();
    }

    private WordBookDO findWordBook(Long bookId) {
        WordBookDO wordBook = wordBookMapper.selectByPrimaryKey(WordBookDO.builder().id(bookId).build());
        if (wordBook == null) {
            throw new BizException("word book not found");
        }
        return wordBook;
    }

    private int normalizePageNumber(Integer pageNumber) {
        return pageNumber == null || pageNumber < 1 ? DEFAULT_PAGE_NUMBER : pageNumber;
    }

    private int normalizePageSize(Integer pageSize, int defaultPageSize) {
        return pageSize == null || pageSize < 1 ? defaultPageSize : pageSize;
    }

    private int offset(int pageNumber, int pageSize) {
        return (pageNumber - 1) * pageSize;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }
}
