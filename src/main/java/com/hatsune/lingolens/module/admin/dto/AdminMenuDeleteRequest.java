package com.hatsune.lingolens.module.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
public class AdminMenuDeleteRequest {

    /**
     * 需要删除的菜单配置主键 ID。
     */
    @NotNull
    private Long menuId;
}
