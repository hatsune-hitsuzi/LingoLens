package com.hatsune.lingolens.infrastructure.oss;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssUploadCommand {

    /**
     * 文件归属用户主键 ID，匿名上传时为空。
     */
    private Long ownerId;

    /**
     * 原始文件名，用于推断文件后缀。
     */
    private String fileName;

    /**
     * 文件 MIME 类型。
     */
    private String contentType;

    /**
     * 对象存储路径前缀。
     */
    private String objectPrefix;

    /**
     * 文件 Base64 内容。
     */
    private String base64Content;

    /**
     * 是否需要对外公开读取。
     */
    private Boolean publicRead;
}
