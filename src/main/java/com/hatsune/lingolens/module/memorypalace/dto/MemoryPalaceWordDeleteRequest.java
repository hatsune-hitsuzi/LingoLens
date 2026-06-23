package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemoryPalaceWordDeleteRequest {

    @NotNull
    private Long palaceWordId;
}
