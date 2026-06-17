package com.hatsune.lingolens.module.memorypalace.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceReorderResponse {

    private ResponseResult result;

    private Integer acceptedCount;
}
