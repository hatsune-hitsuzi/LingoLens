package com.hatsune.lingolens.module.user.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 用户主键 ID，后端内部使用。
     */
    private Long id;

    /**
     * 用户可见 ID，用于展示和登录。
     */
    private String userId;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 用户头像资源 URL。
     */
    private String avatarUrl;

    /**
     * 用户邮箱。
     */
    private String email;

    /**
     * 用户个人简介。
     */
    private String bio;
}
