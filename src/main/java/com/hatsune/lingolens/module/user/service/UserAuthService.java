package com.hatsune.lingolens.module.user.service;

import com.hatsune.lingolens.module.user.dto.LoginResponse;
import com.hatsune.lingolens.module.user.dto.RegisterResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@RequestMapping("/lingolens/user")
public interface UserAuthService {

    /**
     * 接口功能: 用户注册。
     * 入参: requestJson，注册请求 JSON 字符串，包含 nickname、password、avatarUrl。
     * 出参: RegisterResponse，统一包含 result.rCode/result.msg。
     *
     * @param requestJson 注册请求 JSON 字符串
     * @return 注册结果
     */
    @PostMapping("/register")
    RegisterResponse register(@RequestBody String requestJson);

    /**
     * 接口功能: 用户登录，支持 userId 或 nickname 登录。
     * 入参: requestJson，登录请求 JSON 字符串，包含 account、password。
     * 出参: LoginResponse，统一包含 result.rCode/result.msg，登录成功时返回 token、tokenExpireAt。
     *
     * @param requestJson 登录请求 JSON 字符串
     * @return 登录结果
     */
    @PostMapping("/login")
    LoginResponse login(@RequestBody String requestJson);
}
