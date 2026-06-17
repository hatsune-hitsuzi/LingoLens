package com.hatsune.lingolens.module.lens.service;

import com.hatsune.lingolens.module.lens.dto.LensRecognizeResponse;
import com.hatsune.lingolens.module.lens.dto.LensWordDetailResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @author hatsune_hitsuzi
 * @since 2026-05-23
 */
@RequestMapping("/api/lens")
public interface LensService {

    @PostMapping("/recognize")
    LensRecognizeResponse recognize(@RequestBody String requestJson);

    @PostMapping(value = "/recognize/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseBodyEmitter recognizeStream(@RequestBody String requestJson);

    @PostMapping("/word/detail")
    LensWordDetailResponse wordDetail(@RequestBody String requestJson);
}
