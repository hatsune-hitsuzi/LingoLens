package com.hatsune.lingolens.module.lens.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
public class LensWordDetailRequest {

    @Size(max = 128)
    private String word;

    @Size(max = 16)
    private String sourceLanguage;

    @Size(max = 16)
    private String targetLanguage;

    @Size(max = 128)
    private String sceneName;
}
