package com.hatsune.lingolens.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
public class RegisterRequest {

    /**
     * 用户昵称。
     */
    @NotBlank
    @Size(min = 2, max = 32)
    private String nickname;

    /**
     * 注册明文密码，仅用于生成 BCrypt 密文。
     */
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    /**
     * 用户头像资源 URL，由通用上传接口先行生成。
     */
    @NotBlank
    @Size(max = 512)
    private String avatarUrl;

    /**
     * Optional login email captured by the mobile auth screen.
     */
    @Email
    @Size(max = 128)
    private String email;
}
