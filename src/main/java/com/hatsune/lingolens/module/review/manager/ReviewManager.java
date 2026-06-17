package com.hatsune.lingolens.module.review.manager;

import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
import com.hatsune.lingolens.module.memorypalace.mapper.MemoryPalaceWordMapper;
import com.hatsune.lingolens.module.review.bo.ReviewCardBO;
import com.hatsune.lingolens.module.review.bo.ReviewCardsBO;
import com.hatsune.lingolens.module.review.domain.ReviewRecordDO;
import com.hatsune.lingolens.module.review.dto.ReviewAnswerDTO;
import com.hatsune.lingolens.module.review.dto.ReviewCardsRequest;
import com.hatsune.lingolens.module.review.mapper.ReviewRecordMapper;
import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.module.user.domain.UserPreferenceDO;
import com.hatsune.lingolens.module.user.manager.UserPreferenceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewManager {

    private static final String REVIEW_RECORD_ID_BIZ_TYPE = "review_record";

    private final ReviewRecordMapper reviewRecordMapper;
    private final MemoryPalaceWordMapper memoryPalaceWordMapper;
    private final DistributedIdGenerator distributedIdGenerator;
    private final UserPreferenceManager userPreferenceManager;

    public int countDueToday(Long userPkId) {
        Integer count = reviewRecordMapper.countDueToday(userPkId);
        return count == null ? 0 : count;
    }

    public int countAnsweredToday(Long userPkId) {
        Integer count = reviewRecordMapper.countAnsweredToday(userPkId);
        return count == null ? 0 : count;
    }

    public List<String> latestPreviewImageUrls(Long userPkId, int limit) {
        int normalizedLimit = Math.min(Math.max(limit, 1), 10);
        return reviewRecordMapper.listLatestSourceImageUrls(userPkId, normalizedLimit);
    }

    public ReviewCardsBO cards(Long userPkId, ReviewCardsRequest request) {
        int pageSize = normalizePageSize(request == null ? null : request.getPageSize());
        UserPreferenceDO preference = userPreferenceManager.getPreference(userPkId);
        List<ReviewCardBO> cards = reviewRecordMapper.listDueToday(
                userPkId,
                preference.getSourceLanguage(),
                preference.getTargetLanguage(),
                pageSize);
        return ReviewCardsBO.builder()
                .total(countDueToday(userPkId))
                .cards(cards)
                .build();
    }

    public int recordAnswers(Long userPkId, List<ReviewAnswerDTO> answers) {
        if (CollectionUtils.isEmpty(answers)) {
            return 0;
        }
        OffsetDateTime now = OffsetDateTime.now();
        List<ReviewRecordDO> records = new ArrayList<>();
        for (ReviewAnswerDTO answer : answers) {
            Long wordId = answer.getWordId() == null ? answer.getCardId() : answer.getWordId();
            if (wordId == null && answer.getWordAssetId() == null) {
                continue;
            }
            MemoryPalaceWordDO word = wordId == null
                    ? memoryPalaceWordMapper.selectLatestByAsset(userPkId, answer.getWordAssetId())
                    : memoryPalaceWordMapper.selectByPrimaryKey(MemoryPalaceWordDO.builder().id(wordId).build());
            if (word == null || !userPkId.equals(word.getUserPkId())) {
                continue;
            }
            records.add(ReviewRecordDO.builder()
                    .id(distributedIdGenerator.id(REVIEW_RECORD_ID_BIZ_TYPE))
                    .userPkId(userPkId)
                    .wordId(word.getId())
                    .wordAssetId(word.getWordAssetId())
                    .recognized(Boolean.TRUE.equals(answer.getIsRecognized()))
                    .answerTime(parseAnswerTime(answer.getAnswerTime(), now))
                    .createTime(now)
                    .build());
        }
        if (records.isEmpty()) {
            return 0;
        }
        reviewRecordMapper.batchInsert(records);
        return records.size();
    }

    private OffsetDateTime parseAnswerTime(String answerTime, OffsetDateTime fallback) {
        if (answerTime == null || answerTime.isBlank()) {
            return fallback;
        }
        try {
            return OffsetDateTime.parse(answerTime);
        } catch (DateTimeParseException ignored) {
            return fallback;
        }
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 30;
        }
        return Math.min(pageSize, 100);
    }

}
