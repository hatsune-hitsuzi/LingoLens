package com.hatsune.lingolens.module.user.dto;

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
public class UserPreferenceResponse {

    /**
     * 统一响应结果。
     */
    private ResponseResult result;

    /**
     * 源语言编码，例如 en。
     */
    private String sourceLanguage;

    /**
     * 目标语言编码，例如 ja、zh-CN。
     */
    private String targetLanguage;

    /**
     * 语言方向编码，例如 en-zh-CN。
     */
    private String languageDirection;

    /**
     * 语言方向展示名，例如 英 -> 中。
     */
    private String languageDirectionName;

    private String uiLanguage;

    /**
     * 主题模式：system、light、dark。
     */
    private String themeMode;

    /**
     * 当前客户端可展示的语言方向列表，后续新增语言只扩展该列表。
     */
    private List<LanguagePairDTO> languagePairs;
}
