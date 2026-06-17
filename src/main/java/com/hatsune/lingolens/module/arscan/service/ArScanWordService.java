package com.hatsune.lingolens.module.arscan.service;

import com.hatsune.lingolens.module.arscan.dto.ArScanWordPageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-26
 */
@RequestMapping("/api/ar-scan")
public interface ArScanWordService {

    @PostMapping("/word/page")
    ArScanWordPageResponse pageWords(@RequestBody(required = false) String requestJson);
}
