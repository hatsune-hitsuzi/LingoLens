package com.hatsune.lingolens.module.memorypalace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceSearchWordDTO {

    private Long palaceWordId;

    private Long palaceId;

    private String sceneName;

    private String localizedSceneName;

    private String coverUrl;

    private String sourceImageUrl;

    private Long wordAssetId;

    private String displayWord;

    private String translation;

    private String createTime;
}
