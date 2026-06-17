package com.hatsune.lingolens.module.arscan.dto;

import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Data
public class ArScanWordPageRequest {

    private Integer pageNumber;

    private Integer pageSize;

    private String keyword;
}
