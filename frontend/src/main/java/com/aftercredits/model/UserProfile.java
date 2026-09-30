package com.aftercredits.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the current user's cinema profile, including their Film DNA taste metrics.
 */
public record UserProfile(
        String username,
        String handle,
        String cinemaIdentity,
        String bio,
        int totalFilmsWatched,
        int filmsThisYear,
        int listsCount,
        int reviewsWritten,
        Map<String, Integer> genrePercentages,
        List<String> favoriteDirectors
) {
    public UserProfile {
        Objects.requireNonNull(username, "Username must not be null");
        Objects.requireNonNull(handle, "Handle must not be null");
        Objects.requireNonNull(cinemaIdentity, "Cinema Identity must not be null");
        genrePercentages = (genrePercentages == null) ? Map.of() : Map.copyOf(genrePercentages);
        favoriteDirectors = (favoriteDirectors == null) ? List.of() : List.copyOf(favoriteDirectors);
    }
}
