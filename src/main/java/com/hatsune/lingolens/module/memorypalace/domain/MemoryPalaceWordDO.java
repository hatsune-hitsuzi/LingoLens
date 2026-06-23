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
     * 全局单词资产主键 ID。
     */
    @TableField("word_asset_id")
    private Long wordAssetId;

    @TableField("anchor_x_percent")
    private Integer anchorXPercent;

    @TableField("anchor_y_percent")
    private Integer anchorYPercent;

    @TableField(value = "word_key", exist = false)
    private String wordKey;

    /**
     * 单词原文。
     */
    @TableField(value = "word", exist = false)
    private String word;

    /**
     * 单词展示文案。
     */
    @TableField(value = "display_word", exist = false)
    private String displayWord;

    /**
     * 音标。
     */
    @TableField(value = "phonetic", exist = false)
    private String phonetic;

    /**
     * 发音音频 URL。
     */
    @TableField(value = "pronunciation_url", exist = false)
    private String pronunciationUrl;

    /**
     * 词性。
     */
    @TableField(value = "part_of_speech", exist = false)
    private String partOfSpeech;

    /**
     * 翻译。
     */
    @TableField(value = "translation", exist = false)
    private String translation;

    /**
     * 英文释义。
     */
    @TableField(value = "definition", exist = false)
    private String definition;

    /**
     * 例句。
     */
    @TableField(value = "example_sentence", exist = false)
    private String exampleSentence;

    /**
     * 例句翻译。
     */
    @TableField(value = "example_translation", exist = false)
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
