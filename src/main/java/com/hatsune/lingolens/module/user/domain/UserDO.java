package com.hatsune.lingolens.module.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class UserDO {

    /**
     * 用户主键 ID，系统内部使用，不直接暴露给普通用户交互。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 用户可见 ID，用于用户侧展示、登录和后台检索。
     */
    @TableField("user_id")
    private String userId;

    /**
     * 用户昵称。
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 用户头像资源 URL。
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 用户邮箱。
     */
    @TableField("email")
    private String email;

    /**
     * 用户个人简介。
     */
    @TableField("bio")
    private String bio;

    /**
     * BCrypt 加密后的登录密码密文。
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 用户创建时间。
     */
    @TableField("create_time")
    private OffsetDateTime createTime;

    /**
     * 用户最近更新时间。
     */
    @TableField("update_time")
    private OffsetDateTime updateTime;
}
