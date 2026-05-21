package com.hatsune.lingolens.module.wordbook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
public class WordBookListRequest {

    /**
     * 当前页码，从 1 开始。
     */
    @Min(1)
    private Integer pageNumber = 1;

    /**
     * 每页数量，最大 100。
     */
    @Min(1)
    @Max(100)
    private Integer pageSize = 20;

    /**
     * 词库搜索关键字，可匹配词库名称或简介。
     */
    @Size(max = 64)
    private String keyword;

    /**
     * 词库难度等级过滤条件。
     */
    private Integer difficultyLevel;

    /**
     * 词库状态过滤条件，1 表示启用。
     */
    private Integer status;
}
