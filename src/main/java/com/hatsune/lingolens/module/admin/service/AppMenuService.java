package com.hatsune.lingolens.module.admin.service;

import com.hatsune.lingolens.module.admin.dto.AppMenuVisibleListResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@RequestMapping("/api/app/menu")
public interface AppMenuService {

    /**
     * 接口功能: 移动端查询指定页面可见的菜单和 Banner 配置。
     * 入参: requestJson，可见菜单查询请求 JSON 字符串，包含 pageCode、menuType。
     * 出参: AppMenuVisibleListResponse，统一包含 result.rCode/result.msg，成功时返回可见菜单列表。
     *
     * @param requestJson 可见菜单查询请求 JSON 字符串
     * @return 可见菜单列表
     */
    @PostMapping("/visible-list")
    AppMenuVisibleListResponse visibleList(@RequestBody String requestJson);
}
