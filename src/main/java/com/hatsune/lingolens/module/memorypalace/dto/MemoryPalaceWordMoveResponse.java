package com.hatsune.lingolens.module.memorypalace.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemoryPalaceWordMoveResponse {

    private ResponseResult result;

    private Long palaceWordId;

    private Long sourcePalaceId;

    private Long targetPalaceId;

    private Integer sourcePalaceWordCount;

    private Integer targetPalaceWordCount;

    private Boolean moved;
}
