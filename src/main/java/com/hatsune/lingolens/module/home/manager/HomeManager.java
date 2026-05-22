package com.hatsune.lingolens.module.home.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.module.admin.manager.AdminMenuManager;
import com.hatsune.lingolens.module.home.bo.HomeIndexBO;
import com.hatsune.lingolens.module.home.dto.HomeIndexRequest;
import com.hatsune.lingolens.module.memorypalace.manager.MemoryPalaceManager;
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
    private final MemoryPalaceManager memoryPalaceManager;

    public HomeIndexBO getHomeIndex(Long id, HomeIndexRequest request) {
        UserDO user = userMapper.selectByPrimaryKey(UserDO.builder().id(id).build());
        if (user == null) {
            throw new BizException("user not found");
        }
        int scannedWordCount = memoryPalaceManager.countWords(id);
        return HomeIndexBO.builder()
                .user(user)
                .todayNewWordCount(0)
                .todayReviewWordCount(0)
                .learnedWordCount(scannedWordCount)
                .streakDays(0)
                .currentBookName("")
                .dailyGoalWordCount(50)
                .todayFinishedWordCount(0)
                .scannedWordCount(scannedWordCount)
                .todayFinished(false)
                .menuItems(adminMenuManager.listVisible(request.getPageCode(), request.getMenuType()))
                .build();
    }
}
