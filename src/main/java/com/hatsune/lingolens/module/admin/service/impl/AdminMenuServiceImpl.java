package com.hatsune.lingolens.module.admin.service.impl;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.common.response.BaseResponse;
import com.hatsune.lingolens.common.response.ResponseResult;
import com.hatsune.lingolens.common.tools.CommonTools;
import com.hatsune.lingolens.module.admin.bo.AdminMenuListBO;
import com.hatsune.lingolens.module.admin.domain.AdminMenuDO;
import com.hatsune.lingolens.module.admin.dto.AdminMenuCreateRequest;
import com.hatsune.lingolens.module.admin.dto.AdminMenuDeleteRequest;
import com.hatsune.lingolens.module.admin.dto.AdminMenuDetailRequest;
import com.hatsune.lingolens.module.admin.dto.AdminMenuDetailResponse;
import com.hatsune.lingolens.module.admin.dto.AdminMenuItemDTO;
import com.hatsune.lingolens.module.admin.dto.AdminMenuListRequest;
import com.hatsune.lingolens.module.admin.dto.AdminMenuListResponse;
import com.hatsune.lingolens.module.admin.dto.AdminMenuUpdateRequest;
import com.hatsune.lingolens.module.admin.manager.AdminMenuManager;
import com.hatsune.lingolens.module.admin.service.AdminMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
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
public class AdminMenuServiceImpl implements AdminMenuService {

    private final AdminMenuManager adminMenuManager;
    private final CommonTools commonTools;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMenuDetailResponse create(@RequestBody String requestJson) {
        log.info("admin menu create request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            AdminMenuCreateRequest request = commonTools.parseJson(requestJson, AdminMenuCreateRequest.class);
            commonTools.validateByJsr303(request);
            normalizeCreateRequest(request);
            validateCreateRequest(request);
            return buildDetailSuccessResponse(adminMenuManager.create(request));
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("admin menu create failed, reason={}", e.getMessage());
            return buildDetailFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("admin menu create failed by system error", e);
            return buildDetailFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMenuDetailResponse update(@RequestBody String requestJson) {
        log.info("admin menu update request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            AdminMenuUpdateRequest request = commonTools.parseJson(requestJson, AdminMenuUpdateRequest.class);
            commonTools.validateByJsr303(request);
            normalizeUpdateRequest(request);
            validateUpdateRequest(request);
            return buildDetailSuccessResponse(adminMenuManager.update(request));
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("admin menu update failed, reason={}", e.getMessage());
            return buildDetailFailResponse(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("admin menu update failed by system error", e);
            return buildDetailFailResponse("system busy, please try again later");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse delete(@RequestBody String requestJson) {
        log.info("admin menu delete request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            AdminMenuDeleteRequest request = commonTools.parseJson(requestJson, AdminMenuDeleteRequest.class);
            commonTools.validateByJsr303(request);
            Assert.notNull(request.getMenuId(), "menu id must not be null");
            adminMenuManager.delete(request.getMenuId());
            return BaseResponse.success();
        } catch (BizException | IllegalArgumentException e) {
            commonTools.markRollbackOnly();
            log.warn("admin menu delete failed, reason={}", e.getMessage());
            return BaseResponse.fail(e.getMessage());
        } catch (Exception e) {
            commonTools.markRollbackOnly();
            log.error("admin menu delete failed by system error", e);
            return BaseResponse.fail("system busy, please try again later");
        }
    }

    @Override
    public AdminMenuDetailResponse detail(@RequestBody String requestJson) {
        log.info("admin menu detail request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            AdminMenuDetailRequest request = commonTools.parseJson(requestJson, AdminMenuDetailRequest.class);
            commonTools.validateByJsr303(request);
            Assert.notNull(request.getMenuId(), "menu id must not be null");
            return buildDetailSuccessResponse(adminMenuManager.detail(request.getMenuId()));
        } catch (BizException | IllegalArgumentException e) {
            log.warn("admin menu detail failed, reason={}", e.getMessage());
            return buildDetailFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("admin menu detail failed by system error", e);
            return buildDetailFailResponse("system busy, please try again later");
        }
    }

    @Override
    public AdminMenuListResponse list(@RequestBody String requestJson) {
        log.info("admin menu list request, requestJson={}", commonTools.maskSensitiveJson(requestJson));
        try {
            AdminMenuListRequest request = commonTools.parseJsonAllowBlank(requestJson, AdminMenuListRequest.class);
            commonTools.validateByJsr303(request);
            normalizeListRequest(request);
            AdminMenuListBO menuList = adminMenuManager.list(request);
            return buildListSuccessResponse(menuList);
        } catch (BizException | IllegalArgumentException e) {
            log.warn("admin menu list failed, reason={}", e.getMessage());
            return buildListFailResponse(e.getMessage());
        } catch (Exception e) {
            log.error("admin menu list failed by system error", e);
            return buildListFailResponse("system busy, please try again later");
        }
    }

    private void normalizeCreateRequest(AdminMenuCreateRequest request) {
        request.setPageCode(trim(request.getPageCode()));
        request.setMenuCode(trim(request.getMenuCode()));
        request.setMenuName(trim(request.getMenuName()));
        request.setIconUrl(trim(request.getIconUrl()));
        request.setImageUrl(trim(request.getImageUrl()));
        request.setJumpUrl(trim(request.getJumpUrl()));
        request.setRemark(trim(request.getRemark()));
    }

    private void validateCreateRequest(AdminMenuCreateRequest request) {
        Assert.hasText(request.getPageCode(), "page code must not be blank");
        Assert.hasText(request.getMenuCode(), "menu code must not be blank");
        Assert.hasText(request.getMenuName(), "menu name must not be blank");
    }

    private void normalizeUpdateRequest(AdminMenuUpdateRequest request) {
        request.setPageCode(trim(request.getPageCode()));
        request.setMenuCode(trim(request.getMenuCode()));
        request.setMenuName(trim(request.getMenuName()));
        request.setIconUrl(trim(request.getIconUrl()));
        request.setImageUrl(trim(request.getImageUrl()));
        request.setJumpUrl(trim(request.getJumpUrl()));
        request.setRemark(trim(request.getRemark()));
    }

    private void validateUpdateRequest(AdminMenuUpdateRequest request) {
        Assert.notNull(request.getMenuId(), "menu id must not be null");
        Assert.isTrue(hasAnyUpdate(request), "at least one field must be updated");
        if (request.getPageCode() != null) {
            Assert.hasText(request.getPageCode(), "page code must not be blank");
        }
        if (request.getMenuCode() != null) {
            Assert.hasText(request.getMenuCode(), "menu code must not be blank");
        }
        if (request.getMenuName() != null) {
            Assert.hasText(request.getMenuName(), "menu name must not be blank");
        }
    }

    private void normalizeListRequest(AdminMenuListRequest request) {
        request.setKeyword(trim(request.getKeyword()));
        request.setPageCode(trim(request.getPageCode()));
    }

    private boolean hasAnyUpdate(AdminMenuUpdateRequest request) {
        return request.getPageCode() != null
                || request.getMenuCode() != null
                || request.getMenuName() != null
                || request.getMenuType() != null
                || request.getIconUrl() != null
                || request.getImageUrl() != null
                || request.getJumpType() != null
                || request.getJumpUrl() != null
                || request.getVisibleStatus() != null
                || request.getSortNo() != null
                || request.getRemark() != null;
    }

    private AdminMenuDetailResponse buildDetailSuccessResponse(AdminMenuDO menu) {
        return AdminMenuDetailResponse.builder()
                .result(ResponseResult.success())
                .menu(toMenuItem(menu))
                .build();
    }

    private AdminMenuDetailResponse buildDetailFailResponse(String message) {
        return AdminMenuDetailResponse.builder()
                .result(ResponseResult.fail(message))
                .build();
    }

    private AdminMenuListResponse buildListSuccessResponse(AdminMenuListBO menuList) {
        return AdminMenuListResponse.builder()
                .result(ResponseResult.success())
                .pageNumber(menuList.getPageNumber())
                .pageSize(menuList.getPageSize())
                .total(menuList.getTotal())
                .menus(toMenuItems(menuList.getMenus()))
                .build();
    }

    private AdminMenuListResponse buildListFailResponse(String message) {
        return AdminMenuListResponse.builder()
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
