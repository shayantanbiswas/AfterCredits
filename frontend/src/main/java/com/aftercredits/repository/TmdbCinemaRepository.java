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

        // Offline mode keeps the existing seeded fallback state.
        // Live mode starts with a genuinely empty user library.
        if (!isLiveMode()) {
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
    }

    public boolean isLiveMode() {
        return tmdbClient != null && tmdbClient.hasToken();
    }

    @Override
    public List<Movie> getAllMovies() {
        return isLiveMode()
                ? java.util.Collections.emptyList()
                : fallbackRepository.getAllMovies();
    }

    @Override
    public Optional<Movie> getMovieById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        // TMDB list/search results are lightweight records.
        // Always fetch the full record when opening a TMDB movie so
        // director, cast, runtime, tagline and IMDb data are populated.
        if (id.startsWith("tmdb-") && isLiveMode()) {
            try {
                int tmdbId = Integer.parseInt(id.substring(5));
                Optional<Movie> detail = tmdbClient.getMovieDetails(tmdbId);

                if (detail.isPresent()) {
                    Movie detailedMovie = detail.get();
                    movieDetailCache.put(id, detailedMovie);
                    return Optional.of(detailedMovie);
                }
            } catch (Exception e) {
                System.err.println(
                        "[AfterCredits] Full TMDB movie details unavailable ("
                                + e.getMessage() + ")"
                );
            }

            // If the detail request fails, keep the lightweight cached
            // movie so the details screen can still open.
            Movie cached = movieDetailCache.get(id);
            if (cached != null) {
                return Optional.of(cached);
            }

            return Optional.empty();
        }

        // Local/offline records can use the cache directly.
        if (movieDetailCache.containsKey(id)) {
            return Optional.of(movieDetailCache.get(id));
        }

        if (savedMoviesMap.containsKey(id)) {
            return Optional.of(savedMoviesMap.get(id));
        }

        return isLiveMode()
                ? Optional.empty()
                : fallbackRepository.getMovieById(id);
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

        if (!isLiveMode()) {
            return filterByGenre(
                    fallbackRepository.searchMovies(cleanQuery, cleanGenre),
                    cleanGenre
            );
        }

        return java.util.Collections.emptyList();
    }

    @Override
    public List<Movie> getTrendingMovies() {
        try {
            List<Movie> trending = tmdbClient.getTrendingMovies();
            if (!trending.isEmpty()) {
                for (Movie m : trending) {
                    movieDetailCache.putIfAbsent(m.id(), m);
                }
                return trending;
            }
        } catch (Exception e) {
            System.err.println("[AfterCredits] Trending unavailable from TMDB (" + e.getMessage() + "). Using offline trending.");
        }

        return java.util.Collections.emptyList();
    }

    @Override
    public List<Movie> getPopularMovies() {
        try {
            List<Movie> movies = tmdbClient.getPopularMovies();
            if (!movies.isEmpty()) {
                for (Movie m : movies) {
                    movieDetailCache.putIfAbsent(m.id(), m);
                }
                return movies;
            }
        } catch (Exception e) {
            System.err.println("[AfterCredits] Popular movies unavailable from TMDB (" + e.getMessage() + "). Using offline catalogue.");
        }

        return java.util.Collections.emptyList();
    }

    @Override
    public List<Movie> getTopRatedMovies() {
        try {
            List<Movie> movies = tmdbClient.getTopRatedMovies();
            if (!movies.isEmpty()) {
                for (Movie m : movies) {
                    movieDetailCache.putIfAbsent(m.id(), m);
                }
                return movies;
            }
        } catch (Exception e) {
            System.err.println("[AfterCredits] Top rated movies unavailable from TMDB (" + e.getMessage() + "). Using offline catalogue.");
        }

        return java.util.Collections.emptyList();
    }

    @Override
    public List<Movie> getNowPlayingMovies() {
        try {
            List<Movie> movies = tmdbClient.getNowPlayingMovies();
            if (!movies.isEmpty()) {
                for (Movie m : movies) {
                    movieDetailCache.putIfAbsent(m.id(), m);
                }
                return movies;
            }
        } catch (Exception e) {
            System.err.println("[AfterCredits] Now playing movies unavailable from TMDB (" + e.getMessage() + "). Using offline catalogue.");
        }

        return java.util.Collections.emptyList();
    }

    @Override
    public List<Movie> getUpcomingMovies() {
        try {
            List<Movie> movies = tmdbClient.getUpcomingMovies();
            if (!movies.isEmpty()) {
                for (Movie m : movies) {
                    movieDetailCache.putIfAbsent(m.id(), m);
                }
                return movies;
            }
        } catch (Exception e) {
            System.err.println("[AfterCredits] Upcoming movies unavailable from TMDB (" + e.getMessage() + "). Using offline catalogue.");
        }

        return java.util.Collections.emptyList();
    }

    @Override
    public Movie getSpotlightMovie() {
        List<Movie> trending = getTrendingMovies();
        if (!trending.isEmpty()) {
            // Use the live trending record directly for the Home hero.
            // Avoid a second network request that can fail and trigger
            // an unrelated offline/fallback movie.
            return trending.getFirst();
        }
        return isLiveMode()
                ? null
                : fallbackRepository.getSpotlightMovie();
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
