package com.hatsune.lingolens.infrastructure.llm;

import lombok.Builder;
import lombok.Data;

/**
 * Result of an image editing request. The provider may return either base64
 * image bytes or a temporary image URL.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-24
 */
@Data
@Builder
public class ImageEditResult {

    private String base64Content;

    private String contentType;

    private String imageUrl;

    public boolean hasBase64Content() {
        return base64Content != null && !base64Content.isBlank();
    }

    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.isBlank();
    }
}
