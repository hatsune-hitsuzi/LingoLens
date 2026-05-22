package com.hatsune.lingolens.module.memorypalace.domain;

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
 * @since 2026-05-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("memory_palace_words")
public class MemoryPalaceWordDO {

    /**
     * 记忆宫殿单词主键 ID。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 记忆宫殿主键 ID。
     */
    @TableField("palace_id")
    private Long palaceId;

    /**
     * 用户表主键 ID，后端内部使用。
     */
    @TableField("user_pk_id")
    private Long userPkId;

    /**
     * 单词去重键，通常为小写后的原词。
     */
    @TableField("word_key")
    private String wordKey;

    /**
     * 单词原文。
     */
    @TableField("word")
    private String word;

    /**
     * 单词展示文案。
     */
    @TableField("display_word")
    private String displayWord;

    /**
     * 音标。
     */
    @TableField("phonetic")
    private String phonetic;

    /**
     * 发音音频 URL。
     */
    @TableField("pronunciation_url")
    private String pronunciationUrl;

    /**
     * 词性。
     */
    @TableField("part_of_speech")
    private String partOfSpeech;

    /**
     * 翻译。
     */
    @TableField("translation")
    private String translation;

    /**
     * 英文释义。
     */
    @TableField("definition")
    private String definition;

    /**
     * 例句。
     */
    @TableField("example_sentence")
    private String exampleSentence;

    /**
     * 例句翻译。
     */
    @TableField("example_translation")
    private String exampleTranslation;

    /**
     * 来源图片 URL。
     */
    @TableField("source_image_url")
    private String sourceImageUrl;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    private OffsetDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField("update_time")
    private OffsetDateTime updateTime;
}
