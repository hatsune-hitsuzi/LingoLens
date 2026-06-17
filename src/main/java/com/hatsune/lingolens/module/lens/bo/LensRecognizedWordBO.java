package com.hatsune.lingolens.module.lens.bo;

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
public class LensRecognizedWordBO {

    private String recognitionId;

    private Long wordAssetId;

    private String word;

    private String displayWord;

    private Double confidence;

    private Integer x;

    private Integer y;

    private Integer width;

    private Integer height;
}
