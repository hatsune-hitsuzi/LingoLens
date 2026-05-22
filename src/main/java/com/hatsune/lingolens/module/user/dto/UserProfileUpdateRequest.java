package com.hatsune.lingolens.module.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
public class UserProfileUpdateRequest {

    /**
     * 新昵称，不传则不修改。
     */
    @Size(min = 2, max = 32)
    private String nickname;

    /**
     * 新头像资源 URL，不传则不修改。
     */
    @Size(max = 512)
    private String avatarUrl;

    /**
     * 用户邮箱。
     */
    @Email
    @Size(max = 128)
    private String email;

    /**
     * 用户个人简介。
     */
    @Size(max = 256)
    private String bio;
}
