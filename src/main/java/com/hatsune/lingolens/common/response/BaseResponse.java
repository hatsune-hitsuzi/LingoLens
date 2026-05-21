package com.hatsune.lingolens.common.response;

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
public class BaseResponse {

    /**
     * 统一响应结果信息。
     */
    private ResponseResult result;

    public static BaseResponse success() {
        return BaseResponse.builder()
                .result(ResponseResult.success())
                .build();
    }

    public static BaseResponse fail(String msg) {
        return BaseResponse.builder()
                .result(ResponseResult.fail(msg))
                .build();
    }
}
