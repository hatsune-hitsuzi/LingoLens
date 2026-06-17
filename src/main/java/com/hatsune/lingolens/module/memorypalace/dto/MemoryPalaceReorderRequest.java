package com.hatsune.lingolens.module.memorypalace.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MemoryPalaceReorderRequest {

    @NotEmpty
    private List<Long> palaceIds;
}
