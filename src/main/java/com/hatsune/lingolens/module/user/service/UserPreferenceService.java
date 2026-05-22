package com.hatsune.lingolens.module.user.service;

import com.hatsune.lingolens.module.user.dto.UserPreferenceResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@RequestMapping("/api/user/preference")
public interface UserPreferenceService {

    /**
     * 接口功能: 查询当前用户偏好配置。
     * 入参: requestJson，用户偏好查询 JSON 字符串，可为空 JSON。
     * 出参: UserPreferenceResponse，包含 result、sourceLanguage、targetLanguage、themeMode。
     *
     * @param requestJson 用户偏好查询请求 JSON 字符串。
     * @return 用户偏好配置。
     */
    @PostMapping("/info")
    UserPreferenceResponse info(@RequestBody String requestJson);

    /**
     * 接口功能: 修改当前用户偏好配置。
     * 入参: requestJson，用户偏好修改 JSON 字符串，包含 sourceLanguage、targetLanguage、themeMode。
     * 出参: UserPreferenceResponse，包含 result 和修改后的偏好配置。
     *
     * @param requestJson 用户偏好修改请求 JSON 字符串。
     * @return 修改后的用户偏好配置。
     */
    @PostMapping("/update")
    UserPreferenceResponse update(@RequestBody String requestJson);
}
