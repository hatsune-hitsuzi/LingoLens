package com.hatsune.lingolens.module.memorypalace.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemoryPalaceWordDeleteResponse {

    private ResponseResult result;

    private Long palaceWordId;

    private Long palaceId;

    private Integer palaceWordCount;

    private Boolean deleted;
}
