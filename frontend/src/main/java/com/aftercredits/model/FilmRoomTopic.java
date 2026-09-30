package com.aftercredits.model;

import java.util.Objects;

/**
 * Represents a deep-dive discussion topic inside a movie-specific Film Room.
 * Covers story analysis, cinematography, theories, character arcs, and endings with spoiler flags.
 */
public record FilmRoomTopic(
        String id,
        String movieId,
        String movieTitle,
        String topicTitle,
        String category,
        String author,
        String content,
        boolean hasSpoilers,
        int commentCount,
        int upvotes
) {
    public FilmRoomTopic {
        Objects.requireNonNull(id, "Topic ID must not be null");
        Objects.requireNonNull(movieId, "Movie ID must not be null");
        Objects.requireNonNull(movieTitle, "Movie title must not be null");
        Objects.requireNonNull(topicTitle, "Topic title must not be null");
        Objects.requireNonNull(category, "Category must not be null");
        Objects.requireNonNull(content, "Content must not be null");
    }
}
