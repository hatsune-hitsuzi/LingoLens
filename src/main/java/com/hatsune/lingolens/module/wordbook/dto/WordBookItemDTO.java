package com.hatsune.lingolens.module.wordbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordBookItemDTO {

    /**
     * 词库主键 ID。
     */
    private Long bookId;

    /**
     * 词库名称。
     */
    private String bookName;

    /**
     * 词库简介。
     */
    private String description;

    /**
     * 词库封面图片 URL。
     */
    private String coverUrl;

    /**
     * 词库包含的单词数量。
     */
    private Integer wordCount;

    /**
     * 词库难度等级。
     */
    private Integer difficultyLevel;

    /**
     * 词库状态，1 表示启用。
     */
    private Integer status;
}
