package com.hatsune.lingolens.module.wordbook.service;

import com.hatsune.lingolens.module.wordbook.dto.WordBookDetailResponse;
import com.hatsune.lingolens.module.wordbook.dto.WordBookListResponse;
import com.hatsune.lingolens.module.wordbook.dto.WordBookWordPageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@RequestMapping("/api/word-book")
public interface WordBookService {

    /**
     * 接口功能: 分页查询词库列表。
     * 入参: requestJson，词库列表查询请求 JSON 字符串，包含 pageNumber、pageSize、keyword、difficultyLevel、status。
     * 出参: WordBookListResponse，统一包含 result.rCode/result.msg，成功时返回词库分页列表。
     *
     * @param requestJson 词库列表查询请求 JSON 字符串
     * @return 词库分页列表
     */
    @PostMapping("/list")
    WordBookListResponse list(@RequestBody String requestJson);

    /**
     * 接口功能: 查询指定词库详情。
     * 入参: requestJson，词库详情查询请求 JSON 字符串，包含 bookId。
     * 出参: WordBookDetailResponse，统一包含 result.rCode/result.msg，成功时返回词库详情。
     *
     * @param requestJson 词库详情查询请求 JSON 字符串
     * @return 词库详情
     */
    @PostMapping("/detail")
    WordBookDetailResponse detail(@RequestBody String requestJson);

    /**
     * 接口功能: 分页查询指定词库下的单词列表。
     * 入参: requestJson，词库单词分页查询请求 JSON 字符串，包含 bookId、pageNumber、pageSize、keyword。
     * 出参: WordBookWordPageResponse，统一包含 result.rCode/result.msg，成功时返回单词分页列表。
     *
     * @param requestJson 词库单词分页查询请求 JSON 字符串
     * @return 词库单词分页列表
     */
    @PostMapping("/word/page")
    WordBookWordPageResponse pageWords(@RequestBody String requestJson);
}
