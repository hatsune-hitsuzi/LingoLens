package com.hatsune.lingolens.module.oss.service;

import com.hatsune.lingolens.module.oss.dto.OssUploadResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@RequestMapping("/lingolens/oss")
public interface OssUploadService {

    /**
     * 接口功能: 通用文件上传，返回可访问的资源 URL。
     * 入参: requestJson，上传请求 JSON 字符串，包含 fileBase64、fileName、contentType、bizType、objectPrefix。
     * 出参: OssUploadResponse，统一包含 result.rCode/result.msg，上传成功时返回 url。
     *
     * @param requestJson 上传请求 JSON 字符串
     * @return 上传结果
     */
    @PostMapping("/upload")
    OssUploadResponse upload(@RequestBody String requestJson);

    /**
     * 接口功能: 公开图床单点上传，仅允许图片，上传成功后返回所有人可直接访问的 URL。
     * 入参: requestJson，上传请求 JSON 字符串，包含 fileBase64、fileName、contentType。
     * 出参: OssUploadResponse，统一包含 result.rCode/result.msg，上传成功时返回 url。
     *
     * @param requestJson 上传请求 JSON 字符串
     * @return 上传结果
     */
    @PostMapping("/image/upload")
    OssUploadResponse uploadPublicImage(@RequestBody String requestJson);
}
