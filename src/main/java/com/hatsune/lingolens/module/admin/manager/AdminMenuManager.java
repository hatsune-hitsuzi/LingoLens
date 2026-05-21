package com.hatsune.lingolens.module.admin.manager;

import com.hatsune.lingolens.common.exception.BizException;
import com.hatsune.lingolens.infrastructure.id.DistributedIdGenerator;
import com.hatsune.lingolens.module.admin.bo.AdminMenuListBO;
import com.hatsune.lingolens.module.admin.domain.AdminMenuDO;
import com.hatsune.lingolens.module.admin.dto.AdminMenuCreateRequest;
import com.hatsune.lingolens.module.admin.dto.AdminMenuListRequest;
import com.hatsune.lingolens.module.admin.dto.AdminMenuUpdateRequest;
import com.hatsune.lingolens.module.admin.dto.AppMenuVisibleListRequest;
import com.hatsune.lingolens.module.admin.mapper.AdminMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Component
@RequiredArgsConstructor
public class AdminMenuManager {

    private static final String MENU_ID_BIZ_TYPE = "admin-menu";
    private static final int DEFAULT_PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_SORT_NO = 0;
    private static final int DEFAULT_MENU_TYPE = 1;
    private static final int DEFAULT_JUMP_TYPE = 0;
    private static final int DEFAULT_VISIBLE_STATUS = 1;

    private final AdminMenuMapper adminMenuMapper;
    private final DistributedIdGenerator distributedIdGenerator;

    public AdminMenuDO create(AdminMenuCreateRequest request) {
        ensureMenuCodeAvailable(request.getPageCode(), request.getMenuCode(), null);
        Long id = distributedIdGenerator.id(MENU_ID_BIZ_TYPE);
        AdminMenuDO menu = AdminMenuDO.builder()
                .id(id)
                .pageCode(request.getPageCode())
                .menuCode(request.getMenuCode())
                .menuName(request.getMenuName())
                .menuType(defaultIfNull(request.getMenuType(), DEFAULT_MENU_TYPE))
                .iconUrl(emptyToNull(request.getIconUrl()))
                .imageUrl(emptyToNull(request.getImageUrl()))
                .jumpType(defaultIfNull(request.getJumpType(), DEFAULT_JUMP_TYPE))
                .jumpUrl(emptyToNull(request.getJumpUrl()))
                .visibleStatus(defaultIfNull(request.getVisibleStatus(), DEFAULT_VISIBLE_STATUS))
                .sortNo(defaultIfNull(request.getSortNo(), DEFAULT_SORT_NO))
                .remark(emptyToNull(request.getRemark()))
                .build();
        adminMenuMapper.insert(menu);
        return findById(id);
    }

    public AdminMenuDO update(AdminMenuUpdateRequest request) {
        AdminMenuDO current = findById(request.getMenuId());
        String pageCode = StringUtils.hasText(request.getPageCode()) ? request.getPageCode() : current.getPageCode();
        String menuCode = StringUtils.hasText(request.getMenuCode()) ? request.getMenuCode() : current.getMenuCode();
        ensureMenuCodeAvailable(pageCode, menuCode, current.getId());
        AdminMenuDO update = AdminMenuDO.builder()
                .id(request.getMenuId())
                .pageCode(request.getPageCode())
                .menuCode(request.getMenuCode())
                .menuName(request.getMenuName())
                .menuType(request.getMenuType())
                .iconUrl(request.getIconUrl())
                .imageUrl(request.getImageUrl())
                .jumpType(request.getJumpType())
                .jumpUrl(request.getJumpUrl())
                .visibleStatus(request.getVisibleStatus())
                .sortNo(request.getSortNo())
                .remark(request.getRemark())
                .updateTime(OffsetDateTime.now())
                .build();
        adminMenuMapper.updateByPrimaryKey(update);
        return findById(request.getMenuId());
    }

    public void delete(Long menuId) {
        findById(menuId);
        adminMenuMapper.deleteByPrimaryKey(AdminMenuDO.builder().id(menuId).build());
    }

    public AdminMenuDO detail(Long menuId) {
        return findById(menuId);
    }

    public AdminMenuListBO list(AdminMenuListRequest request) {
        int pageNumber = normalizePageNumber(request.getPageNumber());
        int pageSize = normalizePageSize(request.getPageSize());
        String keyword = normalizeKeyword(request.getKeyword());
        Integer total = adminMenuMapper.countMenus(keyword, request.getPageCode(), request.getMenuType(), request.getVisibleStatus());
        List<AdminMenuDO> menus = total == null || total == 0
                ? Collections.emptyList()
                : adminMenuMapper.listMenus(keyword, request.getPageCode(), request.getMenuType(),
                request.getVisibleStatus(), pageSize, offset(pageNumber, pageSize));
        return AdminMenuListBO.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .total(total == null ? 0 : total)
                .menus(menus)
                .build();
    }

    public List<AdminMenuDO> listVisible(AppMenuVisibleListRequest request) {
        return listVisible(request.getPageCode(), request.getMenuType());
    }

    public List<AdminMenuDO> listVisible(String pageCode, Integer menuType) {
        return adminMenuMapper.listVisibleMenus(pageCode, menuType);
    }

    private AdminMenuDO findById(Long menuId) {
        AdminMenuDO menu = adminMenuMapper.selectByPrimaryKey(AdminMenuDO.builder().id(menuId).build());
        if (menu == null) {
            throw new BizException("menu not found");
        }
        return menu;
    }

    private void ensureMenuCodeAvailable(String pageCode, String menuCode, Long currentId) {
        AdminMenuDO existed = adminMenuMapper.selectOne(AdminMenuDO.builder()
                .pageCode(pageCode)
                .menuCode(menuCode)
                .build());
        if (existed != null && !existed.getId().equals(currentId)) {
            throw new BizException("menu code already exists");
        }
    }

    private int normalizePageNumber(Integer pageNumber) {
        return pageNumber == null || pageNumber < 1 ? DEFAULT_PAGE_NUMBER : pageNumber;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private int offset(int pageNumber, int pageSize) {
        return (pageNumber - 1) * pageSize;
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return keyword.trim();
    }

    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
