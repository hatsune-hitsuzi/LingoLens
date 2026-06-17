package com.hatsune.lingolens.module.review.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCardBO {

    private Long cardId;

    private Long palaceId;

    private Long wordAssetId;

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
}
