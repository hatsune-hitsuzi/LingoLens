package com.hatsune.lingolens.module.memorypalace.manager;

import com.hatsune.lingolens.infrastructure.llm.ImageEditResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;

/**
 * Local fallback renderer for the memory-palace crayon marker.
 *
 * @author hatsune_hitsuzi
 * @since 2026-06-24
 */
@Slf4j
@Component
public class MemoryPalaceCrayonOverlayRenderer {

    private static final String CONTENT_TYPE = "image/jpeg";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    public ImageEditResult render(String imageUrl,
                                  String word,
                                  Integer xPercent,
                                  Integer yPercent) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        try {
            byte[] sourceBytes = download(imageUrl);
            BufferedImage source = ImageIO.read(new java.io.ByteArrayInputStream(sourceBytes));
            if (source == null) {
                return null;
            }
            BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = output.createGraphics();
            try {
                graphics.drawImage(source, 0, 0, null);
                drawCrayonOutline(graphics, source.getWidth(), source.getHeight(), word, xPercent, yPercent);
            } finally {
                graphics.dispose();
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ImageIO.write(output, "jpg", buffer);
            return ImageEditResult.builder()
                    .base64Content(Base64.getEncoder().encodeToString(buffer.toByteArray()))
                    .contentType(CONTENT_TYPE)
                    .build();
        } catch (Exception e) {
            log.warn("memory palace local crayon render failed, imageUrl={}", imageUrl, e);
            return null;
        }
    }

    private byte[] download(String imageUrl) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl.trim()))
                .timeout(Duration.ofSeconds(45))
                .header("Accept", "image/*")
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null) {
            throw new IllegalStateException("source image download failed, status=" + response.statusCode());
        }
        return response.body();
    }

    private void drawCrayonOutline(Graphics2D graphics,
                                   int imageWidth,
                                   int imageHeight,
                                   String word,
                                   Integer xPercent,
                                   Integer yPercent) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double cx = imageWidth * clampPercent(xPercent, 50) / 100D;
        double cy = imageHeight * clampPercent(yPercent, 50) / 100D;
        double shortEdge = Math.max(1D, Math.min(imageWidth, imageHeight));
        double radiusX = Math.max(shortEdge * 0.11D, 78D);
        double radiusY = Math.max(shortEdge * 0.075D, 54D);
        Random random = new Random((String.valueOf(word) + imageWidth + "x" + imageHeight).hashCode());

        for (int layer = 0; layer < 5; layer++) {
            float width = 7.5F + layer * 1.2F;
            int alpha = Math.max(42, 132 - layer * 18);
            graphics.setColor(new Color(255, 255, 255, alpha));
            graphics.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.draw(roughOvalPath(cx, cy, radiusX, radiusY, random, layer));
        }

        graphics.setColor(new Color(255, 255, 255, 96));
        graphics.setStroke(new BasicStroke(2.2F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(roughOvalPath(cx, cy, radiusX * 1.07D, radiusY * 1.08D, random, 8));
    }

    private Path2D roughOvalPath(double cx,
                                 double cy,
                                 double radiusX,
                                 double radiusY,
                                 Random random,
                                 int salt) {
        Path2D path = new Path2D.Double();
        int points = 42;
        for (int index = 0; index <= points; index++) {
            double angle = Math.PI * 2D * index / points;
            double jitter = 1D + (random.nextDouble() - 0.5D) * 0.12D + salt * 0.003D;
            double x = cx + Math.cos(angle) * radiusX * jitter;
            double y = cy + Math.sin(angle) * radiusY * jitter;
            if (index == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    private double clampPercent(Integer value, int fallback) {
        int percent = value == null ? fallback : value;
        return Math.max(0, Math.min(100, percent));
    }
}
