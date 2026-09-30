package com.aftercredits.repository;

import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.model.Movie;
import com.aftercredits.model.MovieGroup;
import com.aftercredits.model.Review;
import com.aftercredits.model.UserProfile;

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
    Movie getSpotlightMovie();

    // --- My Cinema (Personal Library) ---
    List<Movie> getWatchedMovies();
    List<Movie> getWatchlist();
    boolean isWatchlisted(String movieId);
    boolean isWatched(String movieId);
    void addToWatchlist(String movieId);
    void removeFromWatchlist(String movieId);
    void logMovieAsWatched(String movieId, double userRating);

    // --- Film Rooms (Thematic Discussion Spaces) ---
    List<FilmRoomTopic> getAllFilmRoomTopics();
    List<FilmRoomTopic> getTopicsForMovie(String movieId);
    void addFilmRoomTopic(FilmRoomTopic topic);

    // --- Movie Groups ---
    List<MovieGroup> getAllMovieGroups();
    void toggleGroupMembership(String groupId);

    // --- Community Reviews ---
    List<Review> getCommunityReviews();
    void addReview(Review review);

    // --- User Profile & Film DNA ---
    UserProfile getUserProfile();
}
