package com.hatsune.lingolens.module.lens.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
public class LensRecognizeRequest {

    @Size(max = 512)
    private String imageUrl;

    @Size(max = 2_800_000)
    private String imageBase64;

    @Size(max = 64)
    private String imageContentType;

    @Size(max = 128)
    private String sceneName;

    @Size(max = 32)
    private String sourceType;

    @Size(max = 128)
    private String clientTraceId;

    @Size(max = 32)
    private String sourceLanguage;

    @Size(max = 32)
    private String targetLanguage;

    @Valid
    @Size(max = 16)
    private List<LensDetectedTagDTO> detectedTags;
}
