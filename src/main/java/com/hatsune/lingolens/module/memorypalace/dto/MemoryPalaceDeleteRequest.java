package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemoryPalaceDeleteRequest {

    @NotNull
    private Long palaceId;
}
