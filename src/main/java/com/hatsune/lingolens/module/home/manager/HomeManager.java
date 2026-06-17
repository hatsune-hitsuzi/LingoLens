package com.hatsune.lingolens.module.home.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.admin.manager.AdminMenuManager;
import com.hatsune.lingolens.module.arscan.manager.ArScanWordManager;
import com.hatsune.lingolens.module.home.bo.HomeIndexBO;
import com.hatsune.lingolens.module.home.dto.HomeIndexRequest;
import com.hatsune.lingolens.module.review.manager.ReviewManager;
import com.hatsune.lingolens.module.user.domain.UserDO;
import com.hatsune.lingolens.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Component
@RequiredArgsConstructor
public class HomeManager {

    private final UserMapper userMapper;
    private final AdminMenuManager adminMenuManager;
    private final ArScanWordManager arScanWordManager;
    private final ReviewManager reviewManager;

    public HomeIndexBO getHomeIndex(Long id, HomeIndexRequest request) {
        UserDO user = userMapper.selectByPrimaryKey(UserDO.builder().id(id).build());
        if (user == null) {
            throw new BizException("user not found");
        }
        int scannedWordCount = arScanWordManager.countWords(id);
        int todayNewWordCount = arScanWordManager.countCreatedToday(id);
        int todayFinishedWordCount = reviewManager.countAnsweredToday(id);
        int todayReviewWordCount = reviewManager.countDueToday(id);
        int dailyGoalWordCount = todayFinishedWordCount + todayReviewWordCount;
        return HomeIndexBO.builder()
                .user(user)
                .todayNewWordCount(todayNewWordCount)
                .todayReviewWordCount(todayReviewWordCount)
                .learnedWordCount(scannedWordCount)
                .streakDays(0)
                .currentBookName("")
                .dailyGoalWordCount(dailyGoalWordCount)
                .todayFinishedWordCount(todayFinishedWordCount)
                .scannedWordCount(scannedWordCount)
                .todayFinished(dailyGoalWordCount > 0 && todayFinishedWordCount >= dailyGoalWordCount)
                .menuItems(adminMenuManager.listVisible(request.getPageCode(), request.getMenuType()))
                .build();
    }
}
