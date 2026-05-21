package com.hatsune.lingolens.module.admin.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppMenuVisibleListResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 当前页面可见菜单和 Banner 配置列表。
     */
    private List<AdminMenuItemDTO> menus;
}
