package com.hatsune.lingolens.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
public class LoginRequest {

    /**
     * 登录账号，支持用户可见 ID 或昵称。
     */
    @NotBlank
    @Size(max = 32)
    private String account;

    /**
     * 登录明文密码，仅用于本次校验，不落库。
     */
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
