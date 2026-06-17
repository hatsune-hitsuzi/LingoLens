package com.hatsune.lingolens.module.review.dto;

import lombok.Data;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Data
public class ReviewAnswerBatchRequest {

    private List<ReviewAnswerDTO> answers;
}
