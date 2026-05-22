package com.hatsune.lingolens.module.home.service.impl;

import com.hatsune.lingolens.common.auth.UserContext;
import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.admin.domain.AdminMenuDO;
import com.hatsune.lingolens.module.admin.dto.AdminMenuItemDTO;
import com.hatsune.lingolens.module.home.bo.HomeIndexBO;
import com.hatsune.lingolens.module.home.dto.HomeIndexRequest;
import com.hatsune.lingolens.module.home.dto.HomeIndexResponse;
import com.hatsune.lingolens.module.home.manager.HomeManager;
import com.hatsune.lingolens.module.home.service.HomeService;
import com.hatsune.lingolens.module.user.domain.UserDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private static final String DEFAULT_PAGE_CODE = "profile";

    private final HomeManager homeManager;
    private final CommonTools commonTools;

    @Override
    public HomeIndexResponse index(@RequestBody String requestJson) {
        log.info("home index request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            HomeIndexRequest request = commonTools.parseJsonAllowBlank(requestJson, HomeIndexRequest.class);
            commonTools.validateByJsr303(request);
            normalizeRequest(request);
            Long id = UserContext.getUserId();
            HomeIndexBO homeIndex = homeManager.getHomeIndex(id, request);
            return buildSuccessResponse(homeIndex);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("home index failed, reason={}", e.getMessage());
            return buildFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("home index failed by system error", e);
            return buildFailResponse("system busy, please try again later");
        }
    }

    private void normalizeRequest(HomeIndexRequest request) {
        if (!StringUtils.hasText(request.getPageCode())) {
            request.setPageCode(DEFAULT_PAGE_CODE);
            return;
        }
        request.setPageCode(request.getPageCode().trim());
    }

    private HomeIndexResponse buildSuccessResponse(HomeIndexBO homeIndex) {
        UserDO user = homeIndex.getUser();
        return HomeIndexResponse.builder()
                .result(ResponseResult.success())
                .id(user.getId())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .bio(user.getBio())
                .todayNewWordCount(homeIndex.getTodayNewWordCount())
                .todayReviewWordCount(homeIndex.getTodayReviewWordCount())
                .learnedWordCount(homeIndex.getLearnedWordCount())
                .streakDays(homeIndex.getStreakDays())
                .currentBookName(homeIndex.getCurrentBookName())
                .dailyGoalWordCount(homeIndex.getDailyGoalWordCount())
                .todayFinishedWordCount(homeIndex.getTodayFinishedWordCount())
                .scannedWordCount(homeIndex.getScannedWordCount())
                .todayFinished(homeIndex.getTodayFinished())
                .menuItems(toMenuItems(homeIndex.getMenuItems()))
                .build();
    }

    private HomeIndexResponse buildFailResponse(String message) {
        return HomeIndexResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private List<AdminMenuItemDTO> toMenuItems(List<AdminMenuDO> menus) {
        return menus.stream()
                .map(this::toMenuItem)
                .toList();
    }

    private AdminMenuItemDTO toMenuItem(AdminMenuDO menu) {
        return AdminMenuItemDTO.builder()
                .menuId(menu.getId())
                .pageCode(menu.getPageCode())
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .menuType(menu.getMenuType())
                .iconUrl(menu.getIconUrl())
                .imageUrl(menu.getImageUrl())
                .jumpType(menu.getJumpType())
                .jumpUrl(menu.getJumpUrl())
                .visibleStatus(menu.getVisibleStatus())
                .sortNo(menu.getSortNo())
                .remark(menu.getRemark())
                .build();
    }
}
