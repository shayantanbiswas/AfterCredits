package com.aftercredits.repository;

import com.aftercredits.client.tmdb.TmdbApiClient;
import com.aftercredits.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying offline fallback behavior, state management,
 * and data integrity of the TmdbCinemaRepository.
 */
class TmdbCinemaRepositoryTest {

    private TmdbCinemaRepository repository;

    @BeforeEach
    void setUp() {
        // Empty token simulates offline demo mode
        TmdbApiClient offlineClient = new TmdbApiClient("");
        repository = new TmdbCinemaRepository(offlineClient);
    }

    @Test
    void testOfflineModeDetection() {
        assertFalse(repository.isLiveMode(), "Repository should identify as offline when no token is present");
    }

    @Test
    void testCatalogFallbackDataAvailable() {
        List<Movie> allMovies = repository.getAllMovies();
        assertNotNull(allMovies);
        assertEquals(10, allMovies.size(), "Should have 10 pre-seeded fallback films");

        Movie spotlight = repository.getSpotlightMovie();
        assertNotNull(spotlight);
        assertTrue(spotlight.isMock(), "Fallback films should be marked with isMock = true");
        assertNotNull(spotlight.getPosterUrl("w500"), "Fallback films should provide real poster URLs");
    }

    @Test
    void testSearchInOfflineMode() {
        List<Movie> results = repository.searchMovies("Parasite", "All");
        assertFalse(results.isEmpty(), "Search should find pre-seeded film in offline mode");
        assertEquals("Parasite", results.getFirst().title());
    }

    @Test
    void testLibraryStateManagement() {
        String testMovieId = "mov-1"; // Blade Runner 2049
        assertTrue(repository.isWatchlisted(testMovieId));

        // Mark as watched
        repository.logMovieAsWatched(testMovieId, 5.0);
        assertFalse(repository.isWatchlisted(testMovieId), "Logging as watched should remove from watchlist");
        assertTrue(repository.isWatched(testMovieId), "Film should be marked as watched");
    }

    @Test
    void testMovieFormattingHelpers() {
        Optional<Movie> movieOpt = repository.getMovieById("mov-2"); // Parasite
        assertTrue(movieOpt.isPresent());
        Movie movie = movieOpt.get();

        assertEquals("2h 12m", movie.getFormattedDuration());
        assertEquals("tt6751668", movie.imdbId());
        assertEquals("https://www.imdb.com/title/tt6751668", movie.getImdbUrl());
        assertTrue(movie.getTmdbRatingFormatted().contains("TMDB"));
    }
}
