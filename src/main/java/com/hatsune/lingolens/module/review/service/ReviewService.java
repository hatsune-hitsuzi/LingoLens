package com.hatsune.lingolens.module.review.service;

import com.hatsune.lingolens.module.review.dto.ReviewAnswerBatchResponse;
import com.hatsune.lingolens.module.review.dto.ReviewCardsResponse;
import com.hatsune.lingolens.module.review.dto.ReviewCountResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@RequestMapping("/api/v1/review")
public interface ReviewService {

    @PostMapping("/count")
    ReviewCountResponse count(@RequestBody(required = false) String requestJson);

    @PostMapping("/cards")
    ReviewCardsResponse cards(@RequestBody(required = false) String requestJson);

    @PostMapping("/answer/batch")
    ReviewAnswerBatchResponse answerBatch(@RequestBody String requestJson);
}
