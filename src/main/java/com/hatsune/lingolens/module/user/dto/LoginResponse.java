package com.hatsune.lingolens.module.user.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 登录成功后签发的 JWT Token。
     */
    private String token;

    /**
     * Token 过期时间，Unix 秒级时间戳。
     */
    private Long tokenExpireAt;
}
