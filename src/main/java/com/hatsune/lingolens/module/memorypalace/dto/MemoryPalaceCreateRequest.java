package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemoryPalaceCreateRequest {

    @NotBlank
    @Size(max = 128)
    private String sceneName;

    @Size(max = 512)
    private String coverUrl;

    @Size(max = 16)
    private String displayLanguage;
}
