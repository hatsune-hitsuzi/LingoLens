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
 * @since 2026-05-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_preferences")
public class UserPreferenceDO {

    /**
     * 用户偏好主键 ID。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 用户表主键 ID，后端内部使用。
     */
    @TableField("user_pk_id")
    private Long userPkId;

    /**
     * 源语言编码，例如 en。
     */
    @TableField("source_language")
    private String sourceLanguage;

    /**
     * 目标语言编码，例如 ja、zh-CN。
     */
    @TableField("target_language")
    private String targetLanguage;

    /**
     * 主题模式：system、light、dark。
     */
    @TableField("theme_mode")
    private String themeMode;

    /**
     * 创建时间。
     */
    @TableField("create_time")
    private OffsetDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField("update_time")
    private OffsetDateTime updateTime;
}
