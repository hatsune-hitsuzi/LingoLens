package com.hatsune.lingolens.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserInfoDTO {

    /**
     * 用户主键 ID，后端内部操作使用。
     */
    private Long id;

    /**
     * 用户可见 ID，用于后台按用户标识检索。
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
     * 用户创建时间。
     */
    private OffsetDateTime createTime;

    /**
     * 用户最近更新时间。
     */
    private OffsetDateTime updateTime;
}
