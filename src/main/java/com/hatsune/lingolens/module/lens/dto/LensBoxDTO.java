package com.hatsune.lingolens.module.lens.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LensBoxDTO {

    private Integer x;

    private Integer y;

    private Integer width;

    private Integer height;
}
