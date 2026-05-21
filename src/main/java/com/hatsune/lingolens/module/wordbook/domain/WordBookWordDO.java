package com.hatsune.lingolens.module.wordbook.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("word_book_words")
public class WordBookWordDO {

    /**
     * 单词主键 ID。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 所属词库主键 ID。
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 单词原文。
     */
    @TableField("word")
    private String word;

    /**
     * 单词音标。
     */
    @TableField("phonetic")
    private String phonetic;

    /**
     * 单词发音音频 URL。
     */
    @TableField("pronunciation_url")
    private String pronunciationUrl;

    /**
     * 单词简要释义或中文翻译。
     */
    @TableField("translation")
    private String translation;

    /**
     * 单词详细英文释义。
     */
    @TableField("definition")
    private String definition;

    /**
     * 示例句子。
     */
    @TableField("example_sentence")
    private String exampleSentence;

    /**
     * 示例句子翻译。
     */
    @TableField("example_translation")
    private String exampleTranslation;

    /**
     * 单词在词库内的排序号。
     */
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 单词记录创建时间。
     */
    @TableField("create_time")
    private OffsetDateTime createTime;

    /**
     * 单词记录最近更新时间。
     */
    @TableField("update_time")
    private OffsetDateTime updateTime;
}
