package com.hatsune.lingolens.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-22
 */
@Data
public class AdminUserQueryByUserIdRequest {

    /**
     * 用户可见 ID，用于反查后端用户主键 ID。
     */
    @NotBlank
    @Size(max = 32)
    private String userId;
}
