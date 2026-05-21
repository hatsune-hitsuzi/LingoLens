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
public class AdminMenuListResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 当前页码。
     */
    private Integer pageNumber;

    /**
     * 每页数量。
     */
    private Integer pageSize;

    /**
     * 符合条件的总记录数。
     */
    private Integer total;

    /**
     * 菜单配置列表。
     */
    private List<AdminMenuItemDTO> menus;
}
