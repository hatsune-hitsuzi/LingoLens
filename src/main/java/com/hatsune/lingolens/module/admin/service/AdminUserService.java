package com.hatsune.lingolens.module.admin.service;

import com.hatsune.lingolens.module.admin.dto.AdminUserQueryResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@RequestMapping("/api/admin/user")
public interface AdminUserService {

    /**
     * 接口功能: 后台通过用户可见 userId 查询后端用户主键 id。
     * 入参: requestJson，用户查询请求 JSON 字符串，包含 userId。
     * 出参: AdminUserQueryResponse，统一包含 result.rCode/result.msg，成功时返回用户主键 id 和基础信息。
     *
     * @param requestJson 用户查询请求 JSON 字符串
     * @return 用户主键 id 和基础信息
     */
    @PostMapping("/query-by-user-id")
    AdminUserQueryResponse queryByUserId(@RequestBody String requestJson);
}
