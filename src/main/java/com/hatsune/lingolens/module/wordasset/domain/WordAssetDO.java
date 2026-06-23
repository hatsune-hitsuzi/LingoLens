package com.hatsune.lingolens.module.wordasset.domain;

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
 * @since 2026-05-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("word_assets")
public class WordAssetDO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("word_key")
    private String wordKey;

    @TableField("canonical_word")
    private String canonicalWord;

    @TableField("language_payloads")
    private String languagePayloads;

    @TableField("asset_image_url")
    private String assetImageUrl;

    @TableField("asset_source")
    private String assetSource;

    @TableField(value = "display_word", exist = false)
    private String displayWord;

    @TableField(value = "phonetic", exist = false)
    private String phonetic;

    @TableField(value = "pronunciation_url", exist = false)
    private String pronunciationUrl;

    @TableField(value = "part_of_speech", exist = false)
    private String partOfSpeech;

    @TableField(value = "translation", exist = false)
    private String translation;

    @TableField(value = "definition", exist = false)
    private String definition;

    @TableField(value = "example_sentence", exist = false)
    private String exampleSentence;

    @TableField(value = "example_translation", exist = false)
    private String exampleTranslation;

    @TableField("create_time")
    private OffsetDateTime createTime;

    @TableField("update_time")
    private OffsetDateTime updateTime;
}
