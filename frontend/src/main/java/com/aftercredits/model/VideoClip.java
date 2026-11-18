package com.aftercredits.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents a video trailer, teaser, scene clip, or behind-the-scenes preview for a movie.
 */
public record VideoClip(
        String id,
        String name,
        String key,
        String site,
        String type,
        boolean official
) {
    public VideoClip {
        id = (id == null || id.isBlank()) ? "clip-" + System.nanoTime() : id;
        name = (name == null || name.isBlank()) ? "Movie Preview" : name.trim();
        site = (site == null || site.isBlank()) ? "YouTube" : site.trim();
        type = (type == null || type.isBlank()) ? "Trailer" : type.trim();
    }

    /**
     * Resolves the full URL to watch this preview.
     */
    public String getPlayUrl(String fallbackMovieTitle) {
        if ("YouTube".equalsIgnoreCase(site) && key != null && !key.isBlank()) {
            return "https://www.youtube.com/watch?v=" + key;
        }
        String searchQuery = (fallbackMovieTitle != null && !fallbackMovieTitle.isBlank())
                ? fallbackMovieTitle + " " + name
                : name;
        return "https://www.youtube.com/results?search_query=" +
                URLEncoder.encode(searchQuery, StandardCharsets.UTF_8);
    }

    /**
     * Resolves YouTube video thumbnail URL if a YouTube key exists.
     */
    public String getThumbnailUrl() {
        if ("YouTube".equalsIgnoreCase(site) && key != null && !key.isBlank()) {
            return "https://img.youtube.com/vi/" + key + "/hqdefault.jpg";
        }
        return null;
    }

    /**
     * Formats the clip category badge (e.g. "OFFICIAL TRAILER", "TEASER", "SCENE CLIP").
     */
    public String getBadgeLabel() {
        if (official && "Trailer".equalsIgnoreCase(type)) {
            return "OFFICIAL TRAILER";
        }
        return type.toUpperCase();
    }
}

