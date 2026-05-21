package com.hatsune.lingolens.module.admin.service.impl;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.admin.domain.AdminMenuDO;
import com.hatsune.lingolens.module.admin.dto.AdminMenuItemDTO;
import com.hatsune.lingolens.module.admin.dto.AppMenuVisibleListRequest;
import com.hatsune.lingolens.module.admin.dto.AppMenuVisibleListResponse;
import com.hatsune.lingolens.module.admin.manager.AdminMenuManager;
import com.hatsune.lingolens.module.admin.service.AppMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class AppMenuServiceImpl implements AppMenuService {

    private final AdminMenuManager adminMenuManager;
    private final CommonTools commonTools;

    @Override
    public AppMenuVisibleListResponse visibleList(@RequestBody String requestJson) {
        log.info("app menu visible list request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            AppMenuVisibleListRequest request = commonTools.parseJsonAllowBlank(requestJson, AppMenuVisibleListRequest.class);
            commonTools.validateByJsr303(request);
            request.setPageCode(trim(request.getPageCode()));
            Assert.hasText(request.getPageCode(), "page code must not be blank");
            return buildSuccessResponse(adminMenuManager.listVisible(request));
        } catch (BizException | IllegalArgumentException e) {
            log.warn("app menu visible list failed, reason={}", e.getMessage());
            return buildFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("app menu visible list failed by system error", e);
            return buildFailResponse("system busy, please try again later");
        }
    }

    private AppMenuVisibleListResponse buildSuccessResponse(List<AdminMenuDO> menus) {
        return AppMenuVisibleListResponse.builder()
                .result(ResponseResult.success())
                .menus(toMenuItems(menus))
                .build();
    }

    private AppMenuVisibleListResponse buildFailResponse(String message) {
        return AppMenuVisibleListResponse.builder()
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
