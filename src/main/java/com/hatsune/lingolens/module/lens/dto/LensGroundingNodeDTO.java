package com.hatsune.lingolens.module.lens.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class LensGroundingNodeDTO {

    private String id;

    private List<List<Double>> polygon;

    private String learningWord;

    private String learningPhonetic;

    private String nativeTranslation;

    private Map<String, String> labels;

    private Map<String, String> phonetics;
}
