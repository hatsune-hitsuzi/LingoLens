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
public class ResponseResult {

    /**
     * 成功业务结果码。
     */
    public static final String SUCCESS_CODE = "0";

    /**
     * 失败业务结果码。
     */
    public static final String FAIL_CODE = "1";

    /**
     * 业务结果码，0 表示成功，1 表示失败。
     */
    private String rCode;

    /**
     * 业务提示信息，可直接用于前端提示。
     */
    private String msg;

    public static ResponseResult success() {
        return success("success");
    }

    public static ResponseResult success(String msg) {
        return ResponseResult.builder()
                .rCode(SUCCESS_CODE)
                .msg(msg)
                .build();
    }

    public static ResponseResult fail(String msg) {
        return ResponseResult.builder()
                .rCode(FAIL_CODE)
                .msg(msg)
                .build();
    }
}
