package com.hatsune.lingolens.module.review.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
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
public class ReviewAnswerBatchResponse {

    private ResponseResult result;

    private Integer acceptedCount;

    private Integer remainingCount;
}
