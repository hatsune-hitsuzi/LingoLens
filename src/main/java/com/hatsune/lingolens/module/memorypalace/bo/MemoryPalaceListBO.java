package com.hatsune.lingolens.module.memorypalace.bo;

import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceListBO {

    /**
     * 当前页码。
     */
    private Integer pageNumber;

    /**
     * 每页条数。
     */
    private Integer pageSize;

    /**
     * 总条数。
     */
    private Integer total;

    /**
     * 记忆宫殿 DO 列表。
     */
    private List<MemoryPalaceDO> palaces;
}
