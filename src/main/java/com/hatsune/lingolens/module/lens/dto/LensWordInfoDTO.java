package com.hatsune.lingolens.module.lens.dto;

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
public class LensWordInfoDTO {

    private Long wordAssetId;

    private String lang;

    private String word;

    private String displayWord;

    private String phonetic;

    private String pronunciationBaseUrl;

    private String pronunciationPath;

    private String pronunciationUrl;

    private String pos;

    private String partOfSpeech;

    private String explanation;

    private String translation;

    private String definition;

    private String example;

    private String exampleSentence;

    private String exampleTranslation;

    private String assetImageUrl;
}
