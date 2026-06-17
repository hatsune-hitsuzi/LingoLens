package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
public class MemoryPalaceWordAddRequest {

    /**
     * 场景名称，例如浅草咖啡馆、书桌、车内。
     */
    @Size(max = 128)
    private String sceneName;

    /**
     * 场景封面或来源图片 URL。
     */
    @Size(max = 512)
    private String sourceImageUrl;

    /**
     * 客户端链路 ID，用于幂等和问题排查。
     */
    @Size(max = 128)
    private String clientTraceId;

    @Min(0)
    @Max(100)
    private Integer xPercent;

    @Min(0)
    @Max(100)
    private Integer yPercent;

    /**
     * 全局单词资产主键 ID。移动端拿到单词详情后优先传这个值。
     */
    private Long wordAssetId;

    /**
     * 单词原文。
     */
    @Size(max = 128)
    private String word;

    /**
     * 单词展示文案。
     */
    @Size(max = 128)
    private String displayWord;

    /**
     * 音标。
     */
    @Size(max = 128)
    private String phonetic;

    /**
     * 发音音频 URL。
     */
    @Size(max = 512)
    private String pronunciationUrl;

    /**
     * 词性。
     */
    @Size(max = 32)
    private String partOfSpeech;

    /**
     * 翻译。
     */
    @Size(max = 512)
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
}
