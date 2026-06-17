package com.hatsune.lingolens.module.review.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCardsResponse {

    private ResponseResult result;

    private Integer total;

    private List<ReviewCardDTO> cards;
}
