package com.hatsune.lingolens.module.lens.dto;

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
public class LensRecognizeResponse {

    private ResponseResult result;

    private Long sceneId;

    private String sceneName;

    private List<LensRecognizedWordDTO> words;
}
