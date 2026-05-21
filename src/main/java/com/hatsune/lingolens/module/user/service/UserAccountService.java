package com.hatsune.lingolens.module.user.service;

import com.hatsune.lingolens.common.response.BaseResponse;
import com.hatsune.lingolens.module.user.dto.UserProfileResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@RequestMapping("/api/user")
public interface UserAccountService {

    /**
     * 接口功能: 当前登录用户退出登录。
     * 入参: requestJson，退出登录请求 JSON 字符串，当前允许传空 JSON 对象。
     * 出参: BaseResponse，统一包含 result.rCode/result.msg。
     *
     * @param requestJson 退出登录请求 JSON 字符串
     * @return 退出登录结果
     */
    @PostMapping("/logout")
    BaseResponse logout(@RequestBody String requestJson);

    /**
     * 接口功能: 获取当前登录用户的个人信息。
     * 入参: requestJson，个人信息查询请求 JSON 字符串，当前允许传空 JSON 对象。
     * 出参: UserProfileResponse，统一包含 result.rCode/result.msg，成功时返回用户公开资料。
     *
     * @param requestJson 个人信息查询请求 JSON 字符串
     * @return 用户个人信息
     */
    @PostMapping("/profile/info")
    UserProfileResponse profileInfo(@RequestBody String requestJson);

    /**
     * 接口功能: 修改当前登录用户的头像、昵称等个人信息。
     * 入参: requestJson，个人信息修改请求 JSON 字符串，包含 nickname、avatarUrl。
     * 出参: UserProfileResponse，统一包含 result.rCode/result.msg，成功时返回更新后的用户公开资料。
     *
     * @param requestJson 个人信息修改请求 JSON 字符串
     * @return 修改后的用户个人信息
     */
    @PostMapping("/profile/update")
    UserProfileResponse updateProfile(@RequestBody String requestJson);

    /**
     * 接口功能: 修改当前登录用户的登录密码。
     * 入参: requestJson，密码修改请求 JSON 字符串，包含 oldPassword、newPassword、confirmPassword。
     * 出参: BaseResponse，统一包含 result.rCode/result.msg。
     *
     * @param requestJson 密码修改请求 JSON 字符串
     * @return 密码修改结果
     */
    @PostMapping("/password/update")
    BaseResponse updatePassword(@RequestBody String requestJson);
}
