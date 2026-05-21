package com.hatsune.lingolens.module.home.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
public class HomeIndexRequest {

    /**
     * 需要获取可见菜单的页面编码，默认 profile。
     */
    @Size(max = 64)
    private String pageCode = "profile";

    /**
     * 菜单类型过滤条件，1 菜单入口，2 Banner。
     */
    private Integer menuType;
}
