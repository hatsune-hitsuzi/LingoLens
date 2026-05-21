package com.hatsune.lingolens.module.wordbook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
public class WordBookWordPageRequest {

    /**
     * 需要查询单词列表的词库主键 ID。
     */
    @NotNull
    private Long bookId;

    /**
     * 当前页码，从 1 开始。
     */
    @Min(1)
    private Integer pageNumber = 1;

    /**
     * 每页数量，最大 200。
     */
    @Min(1)
    @Max(200)
    private Integer pageSize = 50;

    /**
     * 单词搜索关键字，可匹配单词或释义。
     */
    @Size(max = 64)
    private String keyword;
}
