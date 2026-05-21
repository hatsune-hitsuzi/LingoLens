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
public class WordBookWordItemDTO {

    /**
     * 单词主键 ID。
     */
    private Long wordId;

    /**
     * 所属词库主键 ID。
     */
    private Long bookId;

    /**
     * 单词原文。
     */
    private String word;

    /**
     * 单词音标。
     */
    private String phonetic;

    /**
     * 单词发音音频 URL。
     */
    private String pronunciationUrl;

    /**
     * 单词简要释义或中文翻译。
     */
    private String translation;

    /**
     * 单词详细英文释义。
     */
    private String definition;

    /**
     * 示例句子。
     */
    private String exampleSentence;

    /**
     * 示例句子翻译。
     */
    private String exampleTranslation;

    /**
     * 单词在词库内的排序号。
     */
    private Integer sortNo;
}
