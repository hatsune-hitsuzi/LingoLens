package com.hatsune.lingolens.module.home.service;

import com.hatsune.lingolens.module.home.dto.HomeIndexResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@RequestMapping("/api/home")
public interface HomeService {

    /**
     * 接口功能: 获取当前登录用户首页信息，并返回当前页面可见 MenuItem 供移动端渲染。
     * 入参: requestJson，首页查询请求 JSON 字符串，包含 pageCode、menuType，允许传空 JSON 对象。
     * 出参: HomeIndexResponse，统一包含 result.rCode/result.msg，成功时返回首页展示数据和 menuItems。
     *
     * @param requestJson 首页查询请求 JSON 字符串
     * @return 首页汇总信息和可见菜单配置
     */
    @PostMapping("/index")
    HomeIndexResponse index(@RequestBody String requestJson);
}
