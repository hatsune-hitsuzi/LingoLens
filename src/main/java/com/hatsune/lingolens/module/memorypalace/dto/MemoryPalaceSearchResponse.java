package com.hatsune.lingolens.module.memorypalace.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceSearchResponse {

    private ResponseResult result;

    private String keyword;

    private List<MemoryPalaceItemDTO> palaces;

    private List<MemoryPalaceSearchWordDTO> words;
}
