package com.hatsune.lingolens.module.memorypalace.bo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemoryPalaceWordMutationBO {

    private Long palaceWordId;

    private Long sourcePalaceId;

    private Long targetPalaceId;

    private Integer sourcePalaceWordCount;

    private Integer targetPalaceWordCount;

    private Boolean changed;
}
