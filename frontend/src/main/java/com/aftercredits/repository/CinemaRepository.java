package com.aftercredits.repository;

import com.aftercredits.model.CustomWatchlist;
import com.aftercredits.model.ActivityEvent;
import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.model.Movie;
import com.aftercredits.model.MovieGroup;
import com.aftercredits.model.Review;
import com.aftercredits.model.UserProfile;
import com.aftercredits.model.VideoClip;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction for the AfterCredits platform.
 * Follows the Repository pattern (Dependency Inversion Principle), allowing the
 * Java Swing GUI to remain completely decoupled from the underlying data source
 * (Mock in-memory data for Milestone 1, Spring Boot REST API in Milestone 3).
 */
public interface CinemaRepository {

    // --- Movie Catalog & Discovery ---
    List<Movie> getAllMovies();
    Optional<Movie> getMovieById(String id);
    List<Movie> searchMovies(String query, String genreFilter);
    List<Movie> getTrendingMovies();
    List<Movie> getPopularMovies();
    List<Movie> getTopRatedMovies();
    List<Movie> getNowPlayingMovies();
    List<Movie> getUpcomingMovies();
    Movie getSpotlightMovie();
    default String getMovieTrailerUrl(Movie movie) {
        return (movie != null) ? movie.getTrailerUrl() : "";
    }
    List<VideoClip> getMoviePreviews(Movie movie);

    // --- My Cinema (Personal Library & Watchlists) ---
    List<Movie> getWatchedMovies();
    List<Movie> getWatchlist();
    boolean isWatchlisted(String movieId);
    boolean isWatched(String movieId);
    void addToWatchlist(String movieId);
    void removeFromWatchlist(String movieId);
    void logMovieAsWatched(String movieId, double userRating);
    default String getWatchedDate(String movieId) { return ""; }
    default List<Movie> getFavouriteMovies() { return java.util.Collections.emptyList(); }
    default boolean isFavourite(String movieId) { return false; }
    default void toggleFavourite(String movieId) {}
    default List<ActivityEvent> getRecentActivity() { return java.util.Collections.emptyList(); }

    // --- Custom Watchlists ---
    List<CustomWatchlist> getCustomWatchlists();
    Optional<CustomWatchlist> getCustomWatchlist(String id);
    CustomWatchlist createCustomWatchlist(String name, String description, boolean isPrivate);
    boolean deleteCustomWatchlist(String id);
    boolean addMovieToCustomWatchlist(String watchlistId, String movieId);
    boolean removeMovieFromCustomWatchlist(String watchlistId, String movieId);
    boolean setCustomWatchlistPrivacy(String watchlistId, boolean isPrivate);
    List<Movie> getMoviesForCustomWatchlist(String watchlistId);
    List<CustomWatchlist> getWatchlistsContainingMovie(String movieId);

    // --- Film Rooms (Thematic Discussion Spaces) ---
    List<FilmRoomTopic> getAllFilmRoomTopics();
    List<FilmRoomTopic> getTopicsForMovie(String movieId);
    void addFilmRoomTopic(FilmRoomTopic topic);

    // --- Movie Groups ---
    List<MovieGroup> getAllMovieGroups();
    void toggleGroupMembership(String groupId);

    // --- Personal account analytics ---
    default List<Review> getPersonalReviews() {
        return java.util.Collections.emptyList();
    }

    default java.util.Map<String, Double> getPersonalRatings() {
        return java.util.Collections.emptyMap();
    }

    // --- Community Reviews ---
    List<Review> getCommunityReviews();
    void addReview(Review review);

    // --- User Profile & Film DNA ---
    UserProfile getUserProfile();
}
