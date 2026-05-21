package com.hatsune.lingolens.module.admin.bo;

import com.hatsune.lingolens.module.admin.domain.AdminMenuDO;
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
public class AdminMenuListBO {

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
     * 菜单配置 DO 列表。
     */
    private List<AdminMenuDO> menus;
}
