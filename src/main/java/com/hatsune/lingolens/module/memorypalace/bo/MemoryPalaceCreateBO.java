package com.hatsune.lingolens.module.memorypalace.bo;

import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceCreateBO {

    private Boolean created;

    private MemoryPalaceDO palace;
}
