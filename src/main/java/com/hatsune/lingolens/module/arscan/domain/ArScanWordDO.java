package com.hatsune.lingolens.module.arscan.domain;

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
@TableName("ar_scan_words")
public class ArScanWordDO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("user_pk_id")
    private Long userPkId;

    @TableField("word_asset_id")
    private Long wordAssetId;

    @TableField(value = "word_key", exist = false)
    private String wordKey;

    @TableField(value = "word", exist = false)
    private String word;

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

    @TableField("source_image_url")
    private String sourceImageUrl;

    @TableField("scene_name")
    private String sceneName;

    @TableField("first_palace_id")
    private Long firstPalaceId;

    @TableField("create_time")
    private OffsetDateTime createTime;

    @TableField("update_time")
    private OffsetDateTime updateTime;
}
