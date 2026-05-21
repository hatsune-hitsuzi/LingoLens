package com.hatsune.lingolens.module.admin.service;

import com.hatsune.lingolens.common.response.BaseResponse;
import com.hatsune.lingolens.module.admin.dto.AdminMenuDetailResponse;
import com.hatsune.lingolens.module.admin.dto.AdminMenuListResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@RequestMapping("/api/admin/menu")
public interface AdminMenuService {

    /**
     * 接口功能: 后台新增 App 页面菜单或 Banner 配置。
     * 入参: requestJson，菜单新增请求 JSON 字符串，包含 pageCode、menuCode、menuName、menuType 等字段。
     * 出参: AdminMenuDetailResponse，统一包含 result.rCode/result.msg，成功时返回新增后的菜单配置。
     *
     * @param requestJson 菜单新增请求 JSON 字符串
     * @return 新增后的菜单配置
     */
    @PostMapping("/create")
    AdminMenuDetailResponse create(@RequestBody String requestJson);

    /**
     * 接口功能: 后台修改 App 页面菜单或 Banner 配置。
     * 入参: requestJson，菜单修改请求 JSON 字符串，包含 menuId 以及需要修改的字段。
     * 出参: AdminMenuDetailResponse，统一包含 result.rCode/result.msg，成功时返回修改后的菜单配置。
     *
     * @param requestJson 菜单修改请求 JSON 字符串
     * @return 修改后的菜单配置
     */
    @PostMapping("/update")
    AdminMenuDetailResponse update(@RequestBody String requestJson);

    /**
     * 接口功能: 后台删除 App 页面菜单或 Banner 配置。
     * 入参: requestJson，菜单删除请求 JSON 字符串，包含 menuId。
     * 出参: BaseResponse，统一包含 result.rCode/result.msg。
     *
     * @param requestJson 菜单删除请求 JSON 字符串
     * @return 删除结果
     */
    @PostMapping("/delete")
    BaseResponse delete(@RequestBody String requestJson);

    /**
     * 接口功能: 后台查询单个 App 页面菜单或 Banner 配置详情。
     * 入参: requestJson，菜单详情请求 JSON 字符串，包含 menuId。
     * 出参: AdminMenuDetailResponse，统一包含 result.rCode/result.msg，成功时返回菜单配置详情。
     *
     * @param requestJson 菜单详情请求 JSON 字符串
     * @return 菜单配置详情
     */
    @PostMapping("/detail")
    AdminMenuDetailResponse detail(@RequestBody String requestJson);

    /**
     * 接口功能: 后台分页查询 App 页面菜单或 Banner 配置。
     * 入参: requestJson，菜单分页查询请求 JSON 字符串，包含 pageNumber、pageSize、pageCode、menuType、visibleStatus、keyword。
     * 出参: AdminMenuListResponse，统一包含 result.rCode/result.msg，成功时返回菜单配置分页列表。
     *
     * @param requestJson 菜单分页查询请求 JSON 字符串
     * @return 菜单配置分页列表
     */
    @PostMapping("/list")
    AdminMenuListResponse list(@RequestBody String requestJson);
}
