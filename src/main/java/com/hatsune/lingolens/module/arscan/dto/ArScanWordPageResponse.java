package com.hatsune.lingolens.module.arscan.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArScanWordPageResponse {

    private ResponseResult result;

    private Integer pageNumber;

    private Integer pageSize;

    private Integer total;

    private List<ArScanWordItemDTO> words;
}
