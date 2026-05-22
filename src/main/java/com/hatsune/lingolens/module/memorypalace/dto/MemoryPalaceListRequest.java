package com.hatsune.lingolens.module.memorypalace.dto;

import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
public class MemoryPalaceListRequest {

    /**
     * 当前页码，从 1 开始。
     */
    private Integer pageNumber = 1;

    /**
     * 每页条数。
     */
    private Integer pageSize = 20;
}
