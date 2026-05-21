package com.hatsune.lingolens.module.wordbook.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordBookWordPageResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 所属词库主键 ID。
     */
    private Long bookId;

    /**
     * 当前页码。
     */
    private Integer pageNumber;

    /**
     * 每页数量。
     */
    private Integer pageSize;

    /**
     * 符合条件的单词总数。
     */
    private Integer total;

    /**
     * 单词分页列表。
     */
    private List<WordBookWordItemDTO> words;
}
