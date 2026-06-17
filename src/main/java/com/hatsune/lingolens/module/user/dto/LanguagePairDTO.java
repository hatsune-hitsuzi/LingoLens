package com.hatsune.lingolens.module.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguagePairDTO {

    private String direction;

    private String sourceLanguage;

    private String targetLanguage;

    private String label;
}
