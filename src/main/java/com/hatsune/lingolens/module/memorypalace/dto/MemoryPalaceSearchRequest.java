package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemoryPalaceSearchRequest {

    @Size(max = 128)
    private String keyword;

    private Integer limit = 20;

    @Size(max = 16)
    private String displayLanguage;
}
