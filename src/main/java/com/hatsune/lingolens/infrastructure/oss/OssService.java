package com.hatsune.lingolens.infrastructure.oss;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-21
 */
public interface OssService {

    /**
     * 接口功能: 上传文件到对象存储并返回访问地址。
     * 入参: command，上传命令对象，包含文件内容、文件名、内容类型和对象前缀。
     * 出参: String，上传成功后的资源访问 URL。
     *
     * @param command 上传命令对象
     * @return 资源访问 URL
     */
    String upload(OssUploadCommand command);
}
