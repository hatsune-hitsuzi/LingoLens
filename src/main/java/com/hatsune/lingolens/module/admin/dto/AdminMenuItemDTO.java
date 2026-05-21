package com.hatsune.lingolens.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMenuItemDTO {

    /**
     * 菜单配置主键 ID。
     */
    private Long menuId;

    /**
     * 页面编码，例如 profile 表示个人信息页。
     */
    private String pageCode;

    /**
     * 菜单编码，同一页面内唯一。
     */
    private String menuCode;

    /**
     * 菜单或 Banner 展示名称。
     */
    private String menuName;

    /**
     * 菜单类型，1 表示菜单入口，2 表示 Banner。
     */
    private Integer menuType;

    /**
     * 菜单图标 URL。
     */
    private String iconUrl;

    /**
     * Banner 或运营图片 URL。
     */
    private String imageUrl;

    /**
     * 跳转类型，0 不跳转，1 App 路由，2 H5 链接。
     */
    private Integer jumpType;

    /**
     * 跳转地址，含 App 路由或外部 H5 URL。
     */
    private String jumpUrl;

    /**
     * 可见状态，1 可见，0 隐藏。
     */
    private Integer visibleStatus;

    /**
     * 排序号，数值越小越靠前。
     */
    private Integer sortNo;

    /**
     * 后台备注说明。
     */
    private String remark;
}
