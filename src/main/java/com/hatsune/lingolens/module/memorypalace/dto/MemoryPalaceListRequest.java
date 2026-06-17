package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.Size;
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

    @Size(max = 16)
    private String displayLanguage;
}
