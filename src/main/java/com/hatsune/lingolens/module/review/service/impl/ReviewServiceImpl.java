package com.hatsune.lingolens.module.review.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.review.bo.ReviewCardBO;
import com.hatsune.lingolens.module.review.bo.ReviewCardsBO;
import com.hatsune.lingolens.module.review.dto.ReviewAnswerBatchRequest;
import com.hatsune.lingolens.module.review.dto.ReviewAnswerBatchResponse;
import com.hatsune.lingolens.module.review.dto.ReviewCardDTO;
import com.hatsune.lingolens.module.review.dto.ReviewCardsRequest;
import com.hatsune.lingolens.module.review.dto.ReviewCardsResponse;
import com.hatsune.lingolens.module.review.dto.ReviewCountResponse;
import com.hatsune.lingolens.module.review.manager.ReviewManager;
import com.hatsune.lingolens.module.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewManager reviewManager;
    private final CommonTools commonTools;

    @Override
    public ReviewCountResponse count(@RequestBody(required = false) String requestJson) {
        log.info("review count request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            Long userPkId = UserContext.getUserId();
            int count = reviewManager.countDueToday(userPkId);
            List<String> previewImageUrls = reviewManager.latestPreviewImageUrls(userPkId, 3);
            return ReviewCountResponse.builder()
                    .result(ResponseResult.success())
                    .remainingCount(count)
                    .previewImageUrls(previewImageUrls)
                    .build();
        } catch (BizException | IllegalArgumentException e) {
            log.warn("review count failed, reason={}", e.getMessage());
            return ReviewCountResponse.builder()
                    .result(ResponseResult.fail(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("review count failed by system error", e);
            return ReviewCountResponse.builder()
                    .result(ResponseResult.fail("system busy, please try again later"))
                    .build();
        }
    }

    @Override
    public ReviewCardsResponse cards(@RequestBody(required = false) String requestJson) {
        log.info("review cards request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            ReviewCardsRequest request = commonTools.parseJsonAllowBlank(requestJson, ReviewCardsRequest.class);
            ReviewCardsBO cardsBO = reviewManager.cards(UserContext.getUserId(), request);
            return ReviewCardsResponse.builder()
                    .result(ResponseResult.success())
                    .total(cardsBO.getTotal())
                    .cards(toCardDTOs(cardsBO.getCards()))
                    .build();
        } catch (BizException | IllegalArgumentException e) {
            log.warn("review cards failed, reason={}", e.getMessage());
            return ReviewCardsResponse.builder()
                    .result(ResponseResult.fail(e.getMessage()))
                    .build();
        } catch (Exception e) {
            log.error("review cards failed by system error", e);
            return ReviewCardsResponse.builder()
                    .result(ResponseResult.fail("system busy, please try again later"))
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewAnswerBatchResponse answerBatch(@RequestBody String requestJson) {
        log.info("review answer batch request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            ReviewAnswerBatchRequest request = commonTools.parseJson(requestJson, ReviewAnswerBatchRequest.class);
            Long userPkId = UserContext.getUserId();
            int acceptedCount = reviewManager.recordAnswers(userPkId, request.getAnswers());
            return ReviewAnswerBatchResponse.builder()
                    .result(ResponseResult.success())
                    .acceptedCount(acceptedCount)
                    .remainingCount(reviewManager.countDueToday(userPkId))
                    .build();
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("review answer batch failed, reason={}", e.getMessage());
            return ReviewAnswerBatchResponse.builder()
                    .result(ResponseResult.fail(e.getMessage()))
                    .build();
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("review answer batch failed by system error", e);
            return ReviewAnswerBatchResponse.builder()
                    .result(ResponseResult.fail("system busy, please try again later"))
                    .build();
        }
    }

    private List<ReviewCardDTO> toCardDTOs(List<ReviewCardBO> cards) {
        return cards.stream().map(this::toCardDTO).toList();
    }

    private ReviewCardDTO toCardDTO(ReviewCardBO card) {
        return ReviewCardDTO.builder()
                .cardId(card.getCardId())
                .palaceId(card.getPalaceId())
                .wordAssetId(card.getWordAssetId())
                .sceneName(card.getSceneName())
                .word(card.getWord())
                .displayWord(card.getDisplayWord())
                .phonetic(card.getPhonetic())
                .pronunciationUrl(card.getPronunciationUrl())
                .partOfSpeech(card.getPartOfSpeech())
                .translation(card.getTranslation())
                .definition(card.getDefinition())
                .exampleSentence(card.getExampleSentence())
                .exampleTranslation(card.getExampleTranslation())
                .sourceImageUrl(card.getSourceImageUrl())
                .build();
    }
}
