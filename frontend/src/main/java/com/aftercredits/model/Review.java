package com.aftercredits.model;

import java.util.Objects;

/**
 * Represents a user review and rating for a film.
 */
public record Review(
        String id,
        String movieId,
        String movieTitle,
        String author,
        double rating,
        String comment,
        String dateLogged,
        boolean containsSpoilers
) {
    public Review {
        Objects.requireNonNull(id, "Review ID must not be null");
        Objects.requireNonNull(movieId, "Movie ID must not be null");
        Objects.requireNonNull(movieTitle, "Movie title must not be null");
        Objects.requireNonNull(author, "Author must not be null");
        Objects.requireNonNull(comment, "Comment must not be null");
    }

    /**
     * Formats rating as a star string (e.g., "★ 4.5 / 5.0").
     */
    public String getFormattedRating() {
        return String.format("★ %.1f / 5.0", rating);
    }
}
