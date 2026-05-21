package com.hatsune.lingolens.module.admin.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
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
public class AdminMenuDetailResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 菜单配置详情。
     */
    private AdminMenuItemDTO menu;
}
