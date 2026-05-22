package com.hatsune.lingolens.module.memorypalace.bo;

import com.hatsune.lingolens.module.memorypalace.domain.MemoryPalaceWordDO;
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
public class MemoryPalaceWordPageBO {

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
     * 记忆宫殿单词 DO 列表。
     */
    private List<MemoryPalaceWordDO> words;
}
