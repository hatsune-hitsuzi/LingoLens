package com.hatsune.lingolens.module.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@Data
public class UserPreferenceUpdateRequest {

    /**
     * 源语言编码，例如 en。
     */
    @Size(max = 16)
    private String sourceLanguage;

    /**
     * 目标语言编码，例如 ja、zh-CN。
     */
    @Size(max = 16)
    private String targetLanguage;

    /**
     * 主题模式：system、light、dark。
     */
    @Pattern(regexp = "system|light|dark", message = "theme mode must be system, light or dark")
    private String themeMode;
}
