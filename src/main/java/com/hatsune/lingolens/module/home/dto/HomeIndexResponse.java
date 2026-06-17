package com.hatsune.lingolens.module.home.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.module.admin.dto.AdminMenuItemDTO;
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
public class HomeIndexResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 用户主键 ID，后端内部使用。
     */
    private Long id;

    /**
     * 用户可见 ID。
     */
    private String userId;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 用户头像资源 URL。
     */
    private String avatarUrl;

    /**
     * 用户邮箱。
     */
    private String email;

    /**
     * 用户个人简介。
     */
    private String bio;

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
     * 当前学习集合名称，当前移动端暂不展示。
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
     * 当前页面对移动端可见的菜单和 Banner 配置列表。
     */
    private List<AdminMenuItemDTO> menuItems;
}
