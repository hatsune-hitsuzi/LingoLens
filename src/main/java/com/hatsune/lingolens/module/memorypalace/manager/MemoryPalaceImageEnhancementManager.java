package com.hatsune.lingolens.module.memorypalace.manager;

import com.hatsune.lingolens.infrastructure.llm.ImageEditResult;
import com.hatsune.lingolens.infrastructure.llm.LingoLensImageEditClient;
import com.hatsune.lingolens.module.memorypalace.mapper.MemoryPalaceMapper;
import com.hatsune.lingolens.module.memorypalace.mapper.MemoryPalaceWordMapper;
import com.hatsune.lingolens.module.oss.dto.OssUploadRequest;
import com.hatsune.lingolens.module.oss.manager.OssManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Asynchronously replaces a raw palace photo with an AI/fallback crayon-marked
 * version after the add-word request has already returned.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryPalaceImageEnhancementManager {

    private static final String BIZ_TYPE = "memory-palace-crayon";
    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";

    private final LingoLensImageEditClient imageEditClient;
    private final MemoryPalaceCrayonOverlayRenderer localRenderer;
    private final OssManager ossManager;
    private final MemoryPalaceWordMapper memoryPalaceWordMapper;
    private final MemoryPalaceMapper memoryPalaceMapper;

    @Async("memoryPalaceImageExecutor")
    public void enhanceAfterAdd(Long userPkId,
                                Long palaceId,
                                Long palaceWordId,
                                String originalImageUrl,
                                String targetWord,
                                Integer xPercent,
                                Integer yPercent) {
        if (userPkId == null
                || palaceId == null
                || palaceWordId == null
                || !StringUtils.hasText(originalImageUrl)
                || !StringUtils.hasText(targetWord)) {
            log.info("memory palace image enhancement skipped, reason=missing_args, palaceWordId={}", palaceWordId);
            return;
        }
        try {
            String prompt = buildPrompt(targetWord, xPercent, yPercent);
            ImageEditResult result = imageEditClient.editImage(prompt, originalImageUrl);
            if (result == null || (!result.hasBase64Content() && !result.hasImageUrl())) {
                log.info("memory palace image edit unavailable, fallback=local_crayon, palaceWordId={}", palaceWordId);
                result = localRenderer.render(originalImageUrl, targetWord, xPercent, yPercent);
            }
            if (result == null || (!result.hasBase64Content() && !result.hasImageUrl())) {
                log.warn("memory palace image enhancement skipped, reason=no_output, palaceWordId={}", palaceWordId);
                return;
            }
            String enhancedUrl = result.hasBase64Content()
                    ? uploadEnhancedImage(userPkId, palaceWordId, targetWord, result)
                    : result.getImageUrl();
            if (!StringUtils.hasText(enhancedUrl)) {
                log.warn("memory palace image enhancement skipped, reason=no_enhanced_url, palaceWordId={}", palaceWordId);
                return;
            }
            int updated = memoryPalaceWordMapper.updateSourceImageUrlIfCurrent(
                    userPkId,
                    palaceWordId,
                    originalImageUrl,
                    enhancedUrl);
            memoryPalaceMapper.updateCoverUrlIfCurrent(userPkId, palaceId, originalImageUrl, enhancedUrl);
            log.info("memory palace image enhancement finished, palaceWordId={}, updated={}, enhancedUrl={}",
                    palaceWordId, updated, enhancedUrl);
        } catch (Exception e) {
            log.warn("memory palace image enhancement failed, palaceWordId={}", palaceWordId, e);
        }
    }

    private String buildPrompt(String targetWord, Integer xPercent, Integer yPercent) {
        String word = targetWord.trim();
        return """
                Mixed media art. Based on the provided image, keep the original background unchanged, but add a hand-drawn white crayon outline tracing the specific %s. The outline should look like a playful, textured chalk doodle with a rough sketch style. Minimalist 2D overlay on top of the realistic photo.

                Target object: %s.
                Target anchor: x=%s%%, y=%s%% in the full image, if useful.
                Only add the crayon outline. Do not replace, redraw, move, crop, blur, or stylize the original photo.
                """.formatted(word, word, safePercent(xPercent), safePercent(yPercent));
    }

    private String uploadEnhancedImage(Long userPkId,
                                       Long palaceWordId,
                                       String targetWord,
                                       ImageEditResult result) {
        OssUploadRequest request = new OssUploadRequest();
        request.setFileBase64(result.getBase64Content());
        request.setFileName("palace-crayon-%s-%s.jpg".formatted(
                sanitizeFileName(targetWord),
                palaceWordId));
        request.setContentType(StringUtils.hasText(result.getContentType())
                ? result.getContentType()
                : DEFAULT_CONTENT_TYPE);
        request.setBizType(BIZ_TYPE);
        return ossManager.upload(request, userPkId);
    }

    private int safePercent(Integer value) {
        if (value == null) {
            return 50;
        }
        return Math.max(0, Math.min(100, value));
    }

    private String sanitizeFileName(String value) {
        String sanitized = value == null ? "" : value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "-")
                .replaceAll("^-|-$", "");
        return StringUtils.hasText(sanitized) ? sanitized : "word";
    }
}
