package com.hatsune.lingolens.module.review.domain;

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
 * @since 2026-05-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("review_records")
public class ReviewRecordDO {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("user_pk_id")
    private Long userPkId;

    @TableField("word_id")
    private Long wordId;

    @TableField("word_asset_id")
    private Long wordAssetId;

    @TableField("is_recognized")
    private Boolean recognized;

    @TableField("answer_time")
    private OffsetDateTime answerTime;

    @TableField("create_time")
    private OffsetDateTime createTime;
}
