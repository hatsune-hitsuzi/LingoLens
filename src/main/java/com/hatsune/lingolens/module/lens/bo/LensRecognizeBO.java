package com.hatsune.lingolens.module.lens.bo;

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
public class LensRecognizeBO {

    private Long sceneId;

    private String sceneName;

    private List<LensRecognizedWordBO> words;
}
