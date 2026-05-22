package com.hatsune.lingolens.module.memorypalace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryPalaceItemDTO {

    /**
     * 记忆宫殿主键 ID。
     */
    private Long palaceId;

    /**
     * 场景展示名称。
     */
    private String sceneName;

    /**
     * 场景封面 URL。
     */
    private String coverUrl;

    /**
     * 当前场景单词数量。
     */
    private Integer wordCount;

    /**
     * 最近更新时间。
     */
    private OffsetDateTime updateTime;
}
