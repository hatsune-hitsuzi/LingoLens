package com.hatsune.lingolens.module.home.bo;

import com.hatsune.lingolens.module.admin.domain.AdminMenuDO;
import com.hatsune.lingolens.module.user.domain.UserDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeIndexBO {

    /**
     * 当前登录用户 DO。
     */
    private UserDO user;

    /**
     * 今日计划新学单词数。
     */
    private Integer todayNewWordCount;

    /**
     * 今日计划复习单词数。
     */
    private Integer todayReviewWordCount;

    /**
     * 累计已学习单词数。
     */
    private Integer learnedWordCount;

    /**
     * 连续学习天数。
     */
    private Integer streakDays;

    /**
     * 当前学习词库名称。
     */
    private String currentBookName;

    /**
     * 今日目标单词数。
     */
    private Integer dailyGoalWordCount;

    /**
     * 今日已完成单词数。
     */
    private Integer todayFinishedWordCount;

    /**
     * AR 扫描词本总词数。
     */
    private Integer scannedWordCount;

    /**
     * 今日学习任务是否已完成。
     */
    private Boolean todayFinished;

    /**
     * 当前页面对移动端可见的菜单和 Banner 配置。
     */
    private List<AdminMenuDO> menuItems;
}
