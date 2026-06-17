package com.hatsune.lingolens.module.memorypalace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceWordItemDTO {

    /**
     * 记忆宫殿单词主键 ID。
     */
    private Long palaceWordId;

    /**
     * 记忆宫殿主键 ID。
     */
    private Long palaceId;

    /**
     * 全局单词资产主键 ID。
     */
    private Long wordAssetId;

    private String slotKey;

    private String slotName;

    private String anchorDescription;

    private Boolean imageOptimized;

    private String imageUploadProvider;

    /**
     * 单词原文。
     */
    private String word;

    /**
     * 单词展示文案。
     */
    private String displayWord;

    /**
     * 音标。
     */
    private String phonetic;

    /**
     * 发音音频 URL。
     */
    private String pronunciationUrl;

    /**
     * 词性。
     */
    private String partOfSpeech;

    /**
     * 翻译。
     */
    private String translation;

    /**
     * 英文释义。
     */
    private String definition;

    /**
     * 例句。
     */
    private String exampleSentence;

    /**
     * 例句翻译。
     */
    private String exampleTranslation;

    /**
     * 来源图片 URL。
     */
    private String sourceImageUrl;

    /**
     * 创建时间。
     */
    private String createTime;
}
