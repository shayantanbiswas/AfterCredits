package com.aftercredits.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents a movie in the AfterCredits platform.
 * Supports both live TMDB API metadata and offline fallback records.
 * Implemented as an immutable Java 21 record with defensive copying for collections.
 */
public record Movie(
        String id,
        int tmdbId,
        String title,
        String originalTitle,
        int year,
        String releaseDate,
        String director,
        List<String> cast,
        List<String> genres,
        double rating,
        double tmdbVoteAverage,
        int tmdbVoteCount,
        int durationMinutes,
        String synopsis,
        String tagline,
        String posterPath,
        String backdropPath,
        String imdbId,
        boolean isMock
) {
    /**
     * Compact constructor providing validation, collection defensive copying, and null safety.
     */
    public Movie {
        Objects.requireNonNull(id, "Movie ID must not be null");
        Objects.requireNonNull(title, "Title must not be null");
        director = (director == null || director.isBlank()) ? "Unknown Director" : director;
        originalTitle = (originalTitle == null || originalTitle.isBlank()) ? title : originalTitle;
        releaseDate = (releaseDate == null) ? "" : releaseDate;
        synopsis = (synopsis == null) ? "" : synopsis;
        tagline = (tagline == null) ? "" : tagline;
        cast = (cast == null) ? List.of() : List.copyOf(cast);
        genres = (genres == null) ? List.of() : List.copyOf(genres);
    }

    /**
     * Backward-compatible 9-parameter constructor preserving compatibility with existing code.
     */
    public Movie(
            String id,
            String title,
            int year,
            String director,
            List<String> genres,
            double rating,
            int durationMinutes,
            String synopsis,
            String tagline
    ) {
        this(
                id,
                0,
                title,
                title,
                year,
                String.valueOf(year),
                director,
                List.of(),
                genres,
                rating,
                rating,
                0,
                durationMinutes,
                synopsis,
                tagline,
                null,
                null,
                null,
                true // Explicitly marked as mock/offline data
        );
    }

    /**
     * Formats duration into a human-readable format like "2h 44m".
     */
    public String getFormattedDuration() {
        if (durationMinutes <= 0) {
            return "N/A";
        }
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return minutes + "m";
        }
    }

    /**
     * Joins genres into a display string, e.g., "Sci-Fi • Neo-Noir • Mystery".
     */
    public String getGenresFormatted() {
        return genres.isEmpty() ? "Cinema" : String.join(" • ", genres);
    }

    /**
     * Returns top billed cast members formatted as a comma-separated list.
     */
    public String getCastFormatted() {
        return cast.isEmpty() ? "Not available" : String.join(", ", cast);
    }

    /**
     * Resolves the full URL for the poster image from TMDB CDN (e.g. size "w500" or "w342").
     */
    public String getPosterUrl(String size) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }
        if (posterPath.startsWith("http://") || posterPath.startsWith("https://")) {
            return posterPath;
        }
        String validSize = (size == null || size.isBlank()) ? "w500" : size;
        return "https://image.tmdb.org/t/p/" + validSize + (posterPath.startsWith("/") ? "" : "/") + posterPath;
    }

    /**
     * Resolves the full URL for the panoramic backdrop image from TMDB CDN (e.g. size "w1280" or "w780").
     */
    public String getBackdropUrl(String size) {
        if (backdropPath == null || backdropPath.isBlank()) {
            return null;
        }
        if (backdropPath.startsWith("http://") || backdropPath.startsWith("https://")) {
            return backdropPath;
        }
        String validSize = (size == null || size.isBlank()) ? "w1280" : size;
        return "https://image.tmdb.org/t/p/" + validSize + (backdropPath.startsWith("/") ? "" : "/") + backdropPath;
    }

    /**
     * Formats the official TMDB rating and vote count (e.g., "★ 8.5 (34.2k votes)").
     */
    public String getTmdbRatingFormatted() {
        if (tmdbVoteCount <= 0) {
            return String.format("★ %.1f (TMDB)", tmdbVoteAverage);
        }
        String votesFormatted = tmdbVoteCount >= 1000
                ? String.format("%.1fk", tmdbVoteCount / 1000.0)
                : String.valueOf(tmdbVoteCount);
        return String.format("★ %.1f (TMDB • %s votes)", tmdbVoteAverage, votesFormatted);
    }

    /**
     * Generates the external IMDb web link if an IMDb ID is present.
     */
    public String getImdbUrl() {
        if (imdbId != null && !imdbId.isBlank()) {
            return "https://www.imdb.com/title/" + imdbId;
        }
        return null;
    }
}
