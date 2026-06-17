package com.hatsune.lingolens.module.review.dto;

import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Data
public class ReviewAnswerDTO {

    private Long cardId;

    private Long wordId;

    private Long wordAssetId;

    private String word;

    private Boolean isRecognized;

    private String answerTime;
}
