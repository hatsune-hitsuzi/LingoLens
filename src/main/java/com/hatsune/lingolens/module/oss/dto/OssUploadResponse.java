package com.hatsune.lingolens.module.oss.dto;

import com.hatsune.lingolens.common.response.ResponseResult;
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
public class OssUploadResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    /**
     * 上传成功后的资源访问 URL。
     */
    private String url;
}
