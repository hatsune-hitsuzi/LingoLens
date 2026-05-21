package com.hatsune.lingolens.module.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
public class AdminMenuCreateRequest {

    /**
     * 页面编码，例如 profile 表示个人信息页。
     */
    @NotBlank
    @Size(max = 64)
    private String pageCode;

    /**
     * 菜单编码，同一页面内唯一。
     */
    @NotBlank
    @Size(max = 64)
    private String menuCode;

    /**
     * 菜单或 Banner 展示名称。
     */
    @NotBlank
    @Size(max = 128)
    private String menuName;

    /**
     * 菜单类型，1 表示菜单入口，2 表示 Banner。
     */
    @Min(1)
    private Integer menuType = 1;

    /**
     * 菜单图标 URL。
     */
    @Size(max = 512)
    private String iconUrl;

    /**
     * Banner 或运营图片 URL。
     */
    @Size(max = 512)
    private String imageUrl;

    /**
     * 跳转类型，0 不跳转，1 App 路由，2 H5 链接。
     */
    @Min(0)
    private Integer jumpType = 0;

    /**
     * 跳转地址，含 App 路由或外部 H5 URL。
     */
    @Size(max = 512)
    private String jumpUrl;

    /**
     * 可见状态，1 可见，0 隐藏。
     */
    @Min(0)
    @Max(1)
    private Integer visibleStatus = 1;

    /**
     * 排序号，数值越小越靠前。
     */
    private Integer sortNo = 0;

    /**
     * 后台备注说明。
     */
    @Size(max = 512)
    private String remark;
}
