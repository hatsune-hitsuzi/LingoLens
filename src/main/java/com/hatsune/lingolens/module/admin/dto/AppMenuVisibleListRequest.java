package com.hatsune.lingolens.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
public class AppMenuVisibleListRequest {

    /**
     * 页面编码，例如 profile 表示个人信息页。
     */
    @NotBlank
    @Size(max = 64)
    private String pageCode = "profile";

    /**
     * 菜单类型过滤条件，1 菜单入口，2 Banner。
     */
    private Integer menuType;
}
