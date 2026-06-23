package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemoryPalaceWordMoveRequest {

    @NotNull
    private Long palaceWordId;

    @NotNull
    private Long targetPalaceId;
}
