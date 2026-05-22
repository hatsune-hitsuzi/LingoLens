package com.hatsune.lingolens.module.memorypalace.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceWordAddBO {

    /**
     * 记忆宫殿主键 ID。
     */
    private Long palaceId;

    /**
     * 记忆宫殿单词主键 ID。
     */
    private Long palaceWordId;

    /**
     * 当前记忆宫殿中的单词数量。
     */
    private Integer palaceWordCount;

    /**
     * 是否本次新加入。
     */
    private Boolean added;
}
