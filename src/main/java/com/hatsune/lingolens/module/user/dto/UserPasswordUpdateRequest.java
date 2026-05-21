package com.hatsune.lingolens.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
public class UserPasswordUpdateRequest {

    /**
     * 当前旧密码，用于校验用户身份。
     */
    @NotBlank
    @Size(min = 8, max = 72)
    private String oldPassword;

    /**
     * 新密码，校验通过后会生成 BCrypt 密文。
     */
    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;

    /**
     * 新密码确认值，需要与 newPassword 一致。
     */
    @NotBlank
    @Size(min = 8, max = 72)
    private String confirmPassword;
}
