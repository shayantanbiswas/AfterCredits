package com.aftercredits.repository;

import com.aftercredits.client.tmdb.TmdbApiClient;
import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.model.Movie;
import com.aftercredits.model.MovieGroup;
import com.aftercredits.model.Review;
import com.aftercredits.model.UserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * TMDB-backed implementation of {@link CinemaRepository}.
 * Connects to live TMDB API for search and metadata while maintaining user library
 * interactions in memory. Gracefully falls back to {@link MockCinemaRepository} if network fails.
 */
public class TmdbCinemaRepository implements CinemaRepository {

    private final TmdbApiClient tmdbClient;
    private final MockCinemaRepository fallbackRepository;

    // Cache of full movie details by ID
    private final Map<String, Movie> movieDetailCache = new ConcurrentHashMap<>();

    // User library state
    private final Set<String> watchlistIds = new LinkedHashSet<>();
    private final Map<String, Double> watchedMovieRatings = new LinkedHashMap<>();
    private final Map<String, Movie> savedMoviesMap = new ConcurrentHashMap<>();

    public TmdbCinemaRepository(TmdbApiClient tmdbClient) {
        this.tmdbClient = tmdbClient;
        this.fallbackRepository = new MockCinemaRepository();

        // Seed initial saved movies from fallback
        for (Movie m : fallbackRepository.getAllMovies()) {
            savedMoviesMap.put(m.id(), m);
        }
        for (Movie m : fallbackRepository.getWatchlist()) {
            watchlistIds.add(m.id());
        }
        for (Movie m : fallbackRepository.getWatchedMovies()) {
            watchedMovieRatings.put(m.id(), m.rating());
        }
    }

    public boolean isLiveMode() {
        return tmdbClient != null && tmdbClient.hasToken();
    }

    @Override
    public List<Movie> getAllMovies() {
        return fallbackRepository.getAllMovies();
    }

    @Override
    public Optional<Movie> getMovieById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        // 1. Check local cache
        if (movieDetailCache.containsKey(id)) {
            return Optional.of(movieDetailCache.get(id));
        }
        if (savedMoviesMap.containsKey(id)) {
            return Optional.of(savedMoviesMap.get(id));
        }

        // 2. Query TMDB if it is a TMDB identifier
        if (id.startsWith("tmdb-")) {
            try {
                int tmdbId = Integer.parseInt(id.substring(5));
                Optional<Movie> detail = tmdbClient.getMovieDetails(tmdbId);
                detail.ifPresent(m -> movieDetailCache.put(id, m));
                if (detail.isPresent()) {
                    return detail;
                }
            } catch (Exception ignored) {}
        }

        // 3. Fallback to mock catalog
        return fallbackRepository.getMovieById(id);
    }

    @Override
    public List<Movie> searchMovies(String query, String genreFilter) {
        String cleanQuery = (query == null) ? "" : query.trim();
        String cleanGenre = (genreFilter == null) ? "All" : genreFilter.trim();

        if (cleanQuery.isEmpty()) {
            // Return trending or catalog
            List<Movie> trending = getTrendingMovies();
            return filterByGenre(trending, cleanGenre);
        }

        try {
            // Live TMDB Search
            List<Movie> tmdbResults = tmdbClient.searchMovies(cleanQuery);
            if (!tmdbResults.isEmpty()) {
                for (Movie m : tmdbResults) {
                    movieDetailCache.putIfAbsent(m.id(), m);
                }
                return filterByGenre(tmdbResults, cleanGenre);
            }
        } catch (Exception e) {
            // Graceful degradation: Fall back to local mock search on network/auth error
            System.err.println("[AfterCredits] TMDB Search unavailable (" + e.getMessage() + "). Falling back to offline catalogue.");
        }

        return fallbackRepository.searchMovies(cleanQuery, cleanGenre);
    }

    @Override
    public List<Movie> getTrendingMovies() {
        try {
            List<Movie> trending = tmdbClient.getTrendingMovies();
            if (!trending.isEmpty()) {
                for (Movie m : trending) {
                    movieDetailCache.putIfAbsent(m.id(), m);
                }
                return trending.stream().limit(6).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("[AfterCredits] Trending unavailable from TMDB (" + e.getMessage() + "). Using offline trending.");
        }

        return fallbackRepository.getTrendingMovies();
    }

    @Override
    public Movie getSpotlightMovie() {
        List<Movie> trending = getTrendingMovies();
        if (!trending.isEmpty()) {
            Movie first = trending.getFirst();
            // Enrich with full details (director, backdrop) if possible
            return getMovieById(first.id()).orElse(first);
        }
        return fallbackRepository.getSpotlightMovie();
    }

    private List<Movie> filterByGenre(List<Movie> list, String genre) {
        if (genre.equalsIgnoreCase("All")) {
            return list;
        }
        return list.stream()
                .filter(m -> m.genres().stream().anyMatch(g -> g.equalsIgnoreCase(genre)))
                .collect(Collectors.toList());
    }

    // --- My Cinema User Library State ---

    @Override
    public List<Movie> getWatchedMovies() {
        return watchedMovieRatings.keySet().stream()
                .map(this::getMovieById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Movie> getWatchlist() {
        return watchlistIds.stream()
                .map(this::getMovieById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isWatchlisted(String movieId) {
        return watchlistIds.contains(movieId);
    }

    @Override
    public boolean isWatched(String movieId) {
        return watchedMovieRatings.containsKey(movieId);
    }

    @Override
    public void addToWatchlist(String movieId) {
        watchlistIds.add(movieId);
        getMovieById(movieId).ifPresent(m -> savedMoviesMap.put(movieId, m));
    }

    @Override
    public void removeFromWatchlist(String movieId) {
        watchlistIds.remove(movieId);
    }

    @Override
    public void logMovieAsWatched(String movieId, double userRating) {
        watchedMovieRatings.put(movieId, userRating);
        watchlistIds.remove(movieId);
        getMovieById(movieId).ifPresent(m -> savedMoviesMap.put(movieId, m));
    }

    // --- Social & Profile Delegation ---

    @Override
    public List<FilmRoomTopic> getAllFilmRoomTopics() {
        return fallbackRepository.getAllFilmRoomTopics();
    }

    @Override
    public List<FilmRoomTopic> getTopicsForMovie(String movieId) {
        return fallbackRepository.getTopicsForMovie(movieId);
    }

    @Override
    public void addFilmRoomTopic(FilmRoomTopic topic) {
        fallbackRepository.addFilmRoomTopic(topic);
    }

    @Override
    public List<MovieGroup> getAllMovieGroups() {
        return fallbackRepository.getAllMovieGroups();
    }

    @Override
    public void toggleGroupMembership(String groupId) {
        fallbackRepository.toggleGroupMembership(groupId);
    }

    @Override
    public List<Review> getCommunityReviews() {
        return fallbackRepository.getCommunityReviews();
    }

    @Override
    public void addReview(Review review) {
        fallbackRepository.addReview(review);
    }

    @Override
    public UserProfile getUserProfile() {
        return fallbackRepository.getUserProfile();
    }
}
