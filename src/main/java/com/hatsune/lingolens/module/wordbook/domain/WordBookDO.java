package com.hatsune.lingolens.module.wordbook.domain;

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
@TableName("word_books")
public class WordBookDO {

    /**
     * 词库主键 ID。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 词库名称。
     */
    @TableField("book_name")
    private String bookName;

    /**
     * 词库简介。
     */
    @TableField("description")
    private String description;

    /**
     * 词库封面图片 URL。
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 词库包含的单词数量。
     */
    @TableField("word_count")
    private Integer wordCount;

    /**
     * 词库难度等级。
     */
    @TableField("difficulty_level")
    private Integer difficultyLevel;

    /**
     * 词库状态，1 表示启用。
     */
    @TableField("status")
    private Integer status;

    /**
     * 词库创建时间。
     */
    @TableField("create_time")
    private OffsetDateTime createTime;

    /**
     * 词库最近更新时间。
     */
    @TableField("update_time")
    private OffsetDateTime updateTime;
}
