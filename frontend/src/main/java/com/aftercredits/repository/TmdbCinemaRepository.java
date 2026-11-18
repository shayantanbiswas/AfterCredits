package com.aftercredits.repository;

import com.aftercredits.client.tmdb.TmdbApiClient;
import com.aftercredits.model.CustomWatchlist;
import com.aftercredits.model.ActivityEvent;
import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.model.Movie;
import com.aftercredits.model.MovieGroup;
import com.aftercredits.model.Review;
import com.aftercredits.model.UserProfile;
import com.aftercredits.model.VideoClip;

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
    private final WatchlistManager watchlistManager = WatchlistManager.getInstance();
    private final AccountLibraryStore libraryStore = new AccountLibraryStore();

    // Cache of full movie details by ID
    private final Map<String, Movie> movieDetailCache = new ConcurrentHashMap<>();

    // User library state
    private final Map<String, Double> watchedMovieRatings = new LinkedHashMap<>();
    private final Map<String, Movie> savedMoviesMap = new ConcurrentHashMap<>();

    public TmdbCinemaRepository(TmdbApiClient tmdbClient) {
        this.tmdbClient = tmdbClient;
        this.fallbackRepository = new MockCinemaRepository();

        // Offline mode keeps the existing seeded fallback state.
        // Live mode starts with user library synced from WatchlistManager.
        if (!isLiveMode()) {
            for (Movie m : fallbackRepository.getAllMovies()) {
                savedMoviesMap.put(m.id(), m);
            }
            for (Movie m : fallbackRepository.getWatchlist()) {
                watchlistManager.addToDefaultWatchlist(m.id());
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

    @Override
    public String getMovieTrailerUrl(Movie movie) {
        if (movie == null) {
            return "";
        }
        if (isLiveMode() && movie.tmdbId() > 0) {
            try {
                String key = tmdbClient.getTrailerKey(movie.tmdbId());
                if (key != null && !key.isBlank()) {
                    return "https://www.youtube.com/watch?v=" + key;
                }
            } catch (Exception ignored) {}
        }
        return movie.getTrailerUrl();
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
        watchedMovieRatings.clear();
        watchedMovieRatings.putAll(libraryStore.getWatchedRatings());
        return watchedMovieRatings.keySet().stream()
                .map(this::getMovieById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Movie> getWatchlist() {
        return watchlistManager.getDefaultWatchlistIds().stream()
                .map(this::getMovieById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isWatchlisted(String movieId) {
        return watchlistManager.isDefaultWatchlisted(movieId);
    }

    @Override
    public boolean isWatched(String movieId) {
        watchedMovieRatings.clear();
        watchedMovieRatings.putAll(libraryStore.getWatchedRatings());
        return watchedMovieRatings.containsKey(movieId);
    }

    @Override
    public void addToWatchlist(String movieId) {
        watchlistManager.addToDefaultWatchlist(movieId);
        getMovieById(movieId).ifPresent(m -> savedMoviesMap.put(movieId, m));
    }

    @Override
    public void removeFromWatchlist(String movieId) {
        watchlistManager.removeFromDefaultWatchlist(movieId);
    }

    @Override
    public void logMovieAsWatched(String movieId, double userRating) {
        libraryStore.markWatched(movieId, userRating);
        watchedMovieRatings.clear();
        watchedMovieRatings.putAll(libraryStore.getWatchedRatings());
        watchlistManager.removeFromDefaultWatchlist(movieId);
        getMovieById(movieId).ifPresent(m -> savedMoviesMap.put(movieId, m));
    }

    @Override
    public String getWatchedDate(String movieId) {
        return libraryStore.getWatchedDate(movieId);
    }

    @Override
    public List<Movie> getFavouriteMovies() {
        return libraryStore.getFavouriteIds().stream()
                .map(this::getMovieById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFavourite(String movieId) {
        return libraryStore.isFavourite(movieId);
    }

    @Override
    public void toggleFavourite(String movieId) {
        String title = getMovieById(movieId)
                .map(Movie::title)
                .orElse(movieId);
        libraryStore.toggleFavourite(movieId, title);
    }

    @Override
    public List<ActivityEvent> getRecentActivity() {
        return libraryStore.getActivities();
    }

    // --- Custom Watchlists ---

    @Override
    public List<CustomWatchlist> getCustomWatchlists() {
        return watchlistManager.getCustomWatchlists();
    }

    @Override
    public Optional<CustomWatchlist> getCustomWatchlist(String id) {
        return watchlistManager.getCustomWatchlist(id);
    }

    @Override
    public CustomWatchlist createCustomWatchlist(String name, String description, boolean isPrivate) {
        return watchlistManager.createCustomWatchlist(name, description, isPrivate);
    }

    @Override
    public boolean deleteCustomWatchlist(String id) {
        return watchlistManager.deleteCustomWatchlist(id);
    }

    @Override
    public boolean addMovieToCustomWatchlist(String watchlistId, String movieId) {
        boolean added = watchlistManager.addMovieToCustomWatchlist(watchlistId, movieId);
        if (added) {
            getMovieById(movieId).ifPresent(m -> savedMoviesMap.put(movieId, m));
        }
        return added;
    }

    @Override
    public boolean removeMovieFromCustomWatchlist(String watchlistId, String movieId) {
        return watchlistManager.removeMovieFromCustomWatchlist(watchlistId, movieId);
    }

    @Override
    public boolean setCustomWatchlistPrivacy(String watchlistId, boolean isPrivate) {
        return watchlistManager.setCustomWatchlistPrivacy(watchlistId, isPrivate);
    }

    @Override
    public List<Movie> getMoviesForCustomWatchlist(String watchlistId) {
        Optional<CustomWatchlist> wlOpt = watchlistManager.getCustomWatchlist(watchlistId);
        if (wlOpt.isEmpty()) return Collections.emptyList();
        List<String> movieIds = wlOpt.get().getMovieIds();
        return movieIds.stream()
                .map(this::getMovieById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomWatchlist> getWatchlistsContainingMovie(String movieId) {
        return watchlistManager.getWatchlistsContainingMovie(movieId);
    }

    @Override
    public List<VideoClip> getMoviePreviews(Movie movie) {
        if (movie == null) return Collections.emptyList();
        if (isLiveMode() && movie.tmdbId() > 0) {
            try {
                List<VideoClip> tmdbClips = tmdbClient.getMovieVideos(movie.tmdbId());
                if (tmdbClips != null && !tmdbClips.isEmpty()) {
                    return tmdbClips;
                }
            } catch (Exception ignored) {}
        }
        return fallbackRepository.getMoviePreviews(movie);
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
    public List<Review> getPersonalReviews() {
        return libraryStore.getReviews();
    }

    @Override
    public Map<String, Double> getPersonalRatings() {
        return libraryStore.getWatchedRatings();
    }

    @Override
    public List<Review> getCommunityReviews() {
        List<Review> reviews = new ArrayList<>(libraryStore.getReviews());
        reviews.addAll(fallbackRepository.getCommunityReviews());
        return reviews;
    }

    @Override
    public void addReview(Review review) {
        libraryStore.addReview(review);
    }

    @Override
    public UserProfile getUserProfile() {
        return fallbackRepository.getUserProfile();
    }
}
