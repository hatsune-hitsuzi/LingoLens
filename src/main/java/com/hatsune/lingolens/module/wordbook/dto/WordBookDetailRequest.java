package com.hatsune.lingolens.module.wordbook.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
public class WordBookDetailRequest {

    /**
     * 需要查询详情的词库主键 ID。
     */
    @NotNull
    private Long bookId;
}
