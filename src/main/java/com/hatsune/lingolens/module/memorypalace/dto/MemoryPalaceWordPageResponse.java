package com.hatsune.lingolens.module.memorypalace.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
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
public class MemoryPalaceWordPageResponse {

    /**
     * 统一响应结果。
     */
    private ResponseResult result;

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
     * 单词列表。
     */
    private List<MemoryPalaceWordItemDTO> words;
}
