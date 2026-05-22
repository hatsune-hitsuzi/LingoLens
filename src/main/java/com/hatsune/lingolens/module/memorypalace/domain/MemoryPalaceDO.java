package com.hatsune.lingolens.module.memorypalace.domain;

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
@TableName("memory_palaces")
public class MemoryPalaceDO {

    /**
     * 记忆宫殿主键 ID。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 用户表主键 ID，后端内部使用。
     */
    @TableField("user_pk_id")
    private Long userPkId;

    /**
     * 场景唯一键，同一用户下用于识别同一个记忆宫殿。
     */
    @TableField("scene_key")
    private String sceneKey;

    /**
     * 场景展示名称，例如浅草咖啡馆、书桌、车内。
     */
    @TableField("scene_name")
    private String sceneName;

    /**
     * 场景封面图片 URL。
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 当前场景沉淀的单词数量。
     */
    @TableField("word_count")
    private Integer wordCount;

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
