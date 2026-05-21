package com.hatsune.lingolens.module.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
public class AdminMenuListRequest {

    /**
     * 当前页码，从 1 开始。
     */
    @Min(1)
    private Integer pageNumber = 1;

    /**
     * 每页数量，最大 100。
     */
    @Min(1)
    @Max(100)
    private Integer pageSize = 20;

    /**
     * 搜索关键字，可匹配菜单编码、名称或备注。
     */
    @Size(max = 64)
    private String keyword;

    /**
     * 页面编码过滤条件。
     */
    @Size(max = 64)
    private String pageCode;

    /**
     * 菜单类型过滤条件，1 菜单入口，2 Banner。
     */
    private Integer menuType;

    /**
     * 可见状态过滤条件，1 可见，0 隐藏。
     */
    @Min(0)
    @Max(1)
    private Integer visibleStatus;
}
