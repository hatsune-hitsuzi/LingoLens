package com.hatsune.lingolens.module.admin.domain;

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
 * @since 2026-05-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("app_page_menus")
public class AdminMenuDO {

    /**
     * 菜单配置主键 ID。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 页面编码，例如 profile 表示个人信息页。
     */
    @TableField("page_code")
    private String pageCode;

    /**
     * 菜单编码，同一页面内唯一。
     */
    @TableField("menu_code")
    private String menuCode;

    /**
     * 菜单或 Banner 展示名称。
     */
    @TableField("menu_name")
    private String menuName;

    /**
     * 菜单类型，1 表示菜单入口，2 表示 Banner。
     */
    @TableField("menu_type")
    private Integer menuType;

    /**
     * 菜单图标 URL。
     */
    @TableField("icon_url")
    private String iconUrl;

    /**
     * Banner 或运营图片 URL。
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 跳转类型，0 不跳转，1 App 路由，2 H5 链接。
     */
    @TableField("jump_type")
    private Integer jumpType;

    /**
     * 跳转地址，含 App 路由或外部 H5 URL。
     */
    @TableField("jump_url")
    private String jumpUrl;

    /**
     * 可见状态，1 可见，0 隐藏。
     */
    @TableField("visible_status")
    private Integer visibleStatus;

    /**
     * 排序号，数值越小越靠前。
     */
    @TableField("sort_no")
    private Integer sortNo;

    /**
     * 后台备注说明。
     */
    @TableField("remark")
    private String remark;

    /**
     * 配置创建时间。
     */
    @TableField("create_time")
    private OffsetDateTime createTime;

    /**
     * 配置最近更新时间。
     */
    @TableField("update_time")
    private OffsetDateTime updateTime;
}
