package com.hatsune.lingolens.module.oss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
public class OssUploadRequest {

    /**
     * 文件 Base64 内容，支持纯 Base64 或 data URL 格式。
     */
    @NotBlank
    private String fileBase64;

    /**
     * 原始文件名，用于推断文件后缀。
     */
    @Size(max = 128)
    private String fileName;

    /**
     * 文件 MIME 类型，例如 image/png。
     */
    @Size(max = 64)
    private String contentType;

    /**
     * 业务类型，用于组成对象存储路径。
     */
    @Size(max = 64)
    private String bizType;

    /**
     * 自定义对象存储路径前缀。
     */
    @Size(max = 64)
    private String objectPrefix;
}
