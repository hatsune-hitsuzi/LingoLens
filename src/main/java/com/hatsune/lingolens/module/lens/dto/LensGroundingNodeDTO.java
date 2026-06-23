package com.hatsune.lingolens.module.lens.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LensGroundingNodeDTO {

    private String id;

    private List<List<Double>> polygon;

    private String learningWord;

    private String learningPhonetic;

    private String nativeTranslation;
}
