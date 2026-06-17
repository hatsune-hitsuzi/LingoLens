package com.hatsune.lingolens.module.lens.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Object tag produced by the mobile-side detector, for example YOLO/TFLite.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-12
 */
@Data
public class LensDetectedTagDTO {

    @Size(max = 128)
    private String tag;

    @Size(max = 128)
    private String displayWord;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidence;

    private Integer x;

    private Integer y;

    private Integer width;

    private Integer height;
}
