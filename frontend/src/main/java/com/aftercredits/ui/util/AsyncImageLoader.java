package com.aftercredits.ui.util;

import com.aftercredits.theme.ThemeManager;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Asynchronous image loading and caching utility.
 * Guarantees that network image downloads and decoding never block the Swing Event Dispatch Thread (EDT).
 * Provides two-tier caching (in-memory + local disk) and renders elegant programmatic placeholders
 * without relying on emojis or clip-art.
 */
public final class AsyncImageLoader {

    // Tier 1: In-memory cache for decoded, scaled icons (Key: url + "@" + width + "x" + height)
    private static final ConcurrentHashMap<String, ImageIcon> MEMORY_CACHE = new ConcurrentHashMap<>();

    // Tier 2: Persistent disk cache directory (~/.aftercredits/cache/posters/)
    private static final Path DISK_CACHE_DIR = Path.of(
            System.getProperty("user.home"),
            ".aftercredits",
            "cache",
            "posters"
    );

    // Dedicated background worker pool with daemon threads
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "AfterCredits-ImageLoader");
        thread.setDaemon(true);
        return thread;
    });

    // Native Java 21 HTTP Client with connection timeout
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    static {
        try {
            Files.createDirectories(DISK_CACHE_DIR);
        } catch (Exception ignored) {
            // If disk cache directory cannot be created, fallback to memory-only cache
        }
    }

    private AsyncImageLoader() {
        // Utility class: prevent instantiation
    }

    /**
     * Asynchronously loads, scales, and caches an image from an HTTP URL.
     * Guaranteed to invoke the callback on the Swing Event Dispatch Thread (EDT).
     *
     * @param imageUrl     HTTP URL of the poster or backdrop (e.g. TMDB CDN)
     * @param targetWidth  target width in pixels
     * @param targetHeight target height in pixels
     * @param fallbackText title or label to render inside placeholder if offline/loading
     * @param onLoaded     callback receiving the resulting ImageIcon on the EDT
     */
    public static void loadImage(
            String imageUrl,
            int targetWidth,
            int targetHeight,
            String fallbackText,
            Consumer<ImageIcon> onLoaded
    ) {
        if (imageUrl == null || imageUrl.isBlank()) {
            ImageIcon placeholder = createPlaceholder(targetWidth, targetHeight, fallbackText);
            SwingUtilities.invokeLater(() -> onLoaded.accept(placeholder));
            return;
        }

        String cacheKey = imageUrl + "@" + targetWidth + "x" + targetHeight;

        // 1. Check in-memory cache
        ImageIcon cached = MEMORY_CACHE.get(cacheKey);
        if (cached != null) {
            SwingUtilities.invokeLater(() -> onLoaded.accept(cached));
            return;
        }

        // 2. Offload network/disk I/O to background thread
        EXECUTOR.submit(() -> {
            try {
                BufferedImage original = loadFromDiskOrNetwork(imageUrl);

                if (original != null) {
                    ImageIcon scaled = scaleImage(original, targetWidth, targetHeight);
                    MEMORY_CACHE.put(cacheKey, scaled);
                    SwingUtilities.invokeLater(() -> onLoaded.accept(scaled));
                } else {
                    ImageIcon placeholder = createPlaceholder(targetWidth, targetHeight, fallbackText);
                    SwingUtilities.invokeLater(() -> onLoaded.accept(placeholder));
                }
            } catch (Exception e) {
                ImageIcon placeholder = createPlaceholder(targetWidth, targetHeight, fallbackText);
                SwingUtilities.invokeLater(() -> onLoaded.accept(placeholder));
            }
        });
    }

    /**
     * Reads image from local disk cache, or downloads via HTTP and persists to disk.
     */
    private static BufferedImage loadFromDiskOrNetwork(String imageUrl) {
        String filename = hashUrl(imageUrl) + ".img";
        Path diskPath = DISK_CACHE_DIR.resolve(filename);

        // Check disk cache first
        if (Files.exists(diskPath) && Files.isReadable(diskPath)) {
            try {
                BufferedImage diskImage = ImageIO.read(diskPath.toFile());
                if (diskImage != null) {
                    return diskImage;
                }
            } catch (Exception ignored) {
                // If disk file is corrupted, re-download
            }
        }

        // Download from network using Java 21 HttpClient
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "AfterCredits-Desktop/1.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                try (InputStream in = response.body()) {
                    // Atomically write to disk cache
                    Files.copy(in, diskPath, StandardCopyOption.REPLACE_EXISTING);
                }
                return ImageIO.read(diskPath.toFile());
            }
        } catch (Exception ignored) {
            // Network failure or timeout: return null to trigger graceful placeholder
        }

        return null;
    }

    /**
     * High-quality bicubic image scaling.
     */
    private static ImageIcon scaleImage(BufferedImage src, int width, int height) {
        Image scaled = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buffered.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return new ImageIcon(buffered);
    }

    /**
     * Generates a clean, modern vector/gradient placeholder without using emojis or clapperboard clipart.
     */
    public static ImageIcon createPlaceholder(int width, int height, String title) {
        BufferedImage image = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Sleek midnight slate gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(26, 32, 44),
                0, height, new Color(14, 18, 26)
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(0, 0, width, height, 10, 10);

        // 2. Subtle architectural border
        g2.setColor(new Color(42, 52, 70));
        g2.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

        // 3. Minimalist geometric film frame emblem (Clean lines, NO emojis)
        int centerX = width / 2;
        int centerY = height / 2 - 10;
        int boxSize = Math.min(width, height) / 4;

        g2.setColor(new Color(60, 75, 98));
        g2.drawRoundRect(centerX - boxSize / 2, centerY - boxSize / 2, boxSize, boxSize, 6, 6);
        g2.drawLine(centerX - boxSize / 2, centerY, centerX + boxSize / 2, centerY);
        g2.drawOval(centerX - 4, centerY - 4, 8, 8);

        // 4. Clean typographic label
        if (title != null && !title.isBlank()) {
            g2.setFont(ThemeManager.FONT_SMALL);
            g2.setColor(ThemeManager.TEXT_MUTED);

            FontMetrics fm = g2.getFontMetrics();
            String display = title;
            if (fm.stringWidth(display) > (width - 16)) {
                while (display.length() > 3 && fm.stringWidth(display + "...") > (width - 16)) {
                    display = display.substring(0, display.length() - 1);
                }
                display += "...";
            }

            int textX = (width - fm.stringWidth(display)) / 2;
            int textY = centerY + boxSize / 2 + 20;
            g2.drawString(display, textX, textY);
        }

        g2.dispose();
        return new ImageIcon(image);
    }

    /**
     * Hashes an image URL to produce a safe alphanumeric filename for the local disk cache.
     */
    private static String hashUrl(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(url.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(url.hashCode());
        }
    }
}
