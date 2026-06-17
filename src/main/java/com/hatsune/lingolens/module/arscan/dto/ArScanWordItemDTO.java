package com.hatsune.lingolens.module.arscan.dto;

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
public class ArScanWordItemDTO {

    private Long scanWordId;

    private Long wordAssetId;

    private Long firstPalaceId;

    private String sceneName;

    private String word;

    private String displayWord;

    private String phonetic;

    private String pronunciationUrl;

    private String partOfSpeech;

    private String translation;

    private String definition;

    private String exampleSentence;

    private String exampleTranslation;

    private String sourceImageUrl;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;
}
