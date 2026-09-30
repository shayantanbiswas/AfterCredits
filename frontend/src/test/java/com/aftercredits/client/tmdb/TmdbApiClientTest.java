package com.aftercredits.client.tmdb;

import com.aftercredits.model.Movie;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying TMDB v3 API client behavior:
 * - Missing credentials handling (offline safety)
 * - URL request construction with api_key query parameters (without using real secrets)
 * - Safe JSON mapping of search results and movie details
 */
class TmdbApiClientTest {

    @Test
    void testMissingCredentialsGracefulHandling() throws Exception {
        TmdbApiClient emptyClient = new TmdbApiClient("");
        assertFalse(emptyClient.hasApiKey(), "Client should report no API key when initialized with empty string");
        assertFalse(emptyClient.hasToken(), "Backward compatible alias hasToken() should also return false");

        // When no credentials exist, methods must return empty collections without network calls
        List<Movie> searchResults = emptyClient.searchMovies("Vertigo");
        assertTrue(searchResults.isEmpty(), "Search without key should safely return empty list");

        List<Movie> trendingResults = emptyClient.getTrendingMovies();
        assertTrue(trendingResults.isEmpty(), "Trending without key should safely return empty list");

        Optional<Movie> detail = emptyClient.getMovieDetails(550);
        assertTrue(detail.isEmpty(), "Movie detail query without key should safely return empty Optional");

        TmdbApiClient nullClient = new TmdbApiClient(null);
        assertFalse(nullClient.hasApiKey(), "Client should handle null key gracefully");
    }

    @Test
    void testUrlConstructionWithV3ApiKey() {
        String dummyTestKey = "mock_v3_test_key_123";
        TmdbApiClient client = new TmdbApiClient(dummyTestKey);

        assertTrue(client.hasApiKey());

        // 1. Endpoint with existing query parameters -> appends with '&api_key='
        String searchUrl = client.buildRequestUrl("/search/movie?query=Vertigo&page=1");
        assertTrue(searchUrl.startsWith("https://api.themoviedb.org/3/search/movie?query=Vertigo&page=1"));
        assertTrue(searchUrl.endsWith("&api_key=" + dummyTestKey), "Should append api_key with ampersand delimiter");

        // 2. Endpoint without existing query parameters -> appends with '?api_key='
        String detailUrl = client.buildRequestUrl("/movie/426");
        assertEquals("https://api.themoviedb.org/3/movie/426?api_key=" + dummyTestKey, detailUrl,
                "Should append api_key with question mark delimiter when no other query parameters exist");
    }

    @Test
    void testParseSearchResponseSafely() {
        TmdbApiClient client = new TmdbApiClient("mock_key");

        String mockSearchJson = """
                {
                  "page": 1,
                  "results": [
                    {
                      "id": 5902,
                      "title": "Pather Panchali",
                      "original_title": "পথের পাঁচালী",
                      "overview": "The life of a poor family in rural Bengal.",
                      "release_date": "1955-08-26",
                      "vote_average": 8.1,
                      "vote_count": 420,
                      "poster_path": "/pather_panchali.jpg",
                      "backdrop_path": "/pather_bg.jpg",
                      "genre_ids": [18]
                    },
                    {
                      "id": 426,
                      "title": "Vertigo",
                      "original_title": "Vertigo",
                      "overview": "A retired San Francisco detective...",
                      "release_date": "1958-05-28",
                      "vote_average": 8.2,
                      "vote_count": 3950,
                      "poster_path": null,
                      "backdrop_path": null,
                      "genre_ids": [9648, 10749]
                    }
                  ],
                  "total_results": 2
                }
                """;

        List<Movie> parsed = client.parseSearchResponse(mockSearchJson);
        assertEquals(2, parsed.size());

        // First film: Satyajit Ray's Pather Panchali
        Movie pather = parsed.get(0);
        assertEquals("tmdb-5902", pather.id());
        assertEquals(5902, pather.tmdbId());
        assertEquals("Pather Panchali", pather.title());
        assertEquals("পথের পাঁচালী", pather.originalTitle());
        assertEquals(1955, pather.year());
        assertEquals("1955-08-26", pather.releaseDate());
        assertEquals(8.1, pather.tmdbVoteAverage());
        assertEquals(420, pather.tmdbVoteCount());
        assertFalse(pather.isMock(), "TMDB parsed records must be marked with isMock = false");
        assertEquals("https://image.tmdb.org/t/p/w500/pather_panchali.jpg", pather.getPosterUrl("w500"));
        assertTrue(pather.genres().contains("Drama"));

        // Second film: Alfred Hitchcock's Vertigo (with null poster path)
        Movie vertigo = parsed.get(1);
        assertEquals("tmdb-426", vertigo.id());
        assertNull(vertigo.posterPath());
        assertNull(vertigo.getPosterUrl("w500"), "Null poster path should safely resolve to null URL");
        assertTrue(vertigo.genres().contains("Mystery"));
        assertTrue(vertigo.genres().contains("Romance"));
    }

    @Test
    void testParseMovieDetailWithCreditsAndExternalIds() {
        TmdbApiClient client = new TmdbApiClient("mock_key");

        String mockDetailJson = """
                {
                  "id": 426,
                  "title": "Vertigo",
                  "original_title": "Vertigo",
                  "overview": "A retired detective suffers from acrophobia.",
                  "release_date": "1958-05-28",
                  "runtime": 128,
                  "vote_average": 8.2,
                  "vote_count": 4100,
                  "tagline": "Alfred Hitchcock's masterpiece.",
                  "poster_path": "/vertigo.jpg",
                  "backdrop_path": "/vertigo_bg.jpg",
                  "genres": [
                    { "id": 9648, "name": "Mystery" },
                    { "id": 53, "name": "Thriller" }
                  ],
                  "credits": {
                    "crew": [
                      { "name": "Alfred Hitchcock", "job": "Director" },
                      { "name": "Robert Burks", "job": "Director of Photography" }
                    ],
                    "cast": [
                      { "name": "James Stewart", "character": "John 'Scottie' Ferguson" },
                      { "name": "Kim Novak", "character": "Madeleine Elster" }
                    ]
                  },
                  "external_ids": {
                    "imdb_id": "tt0052357"
                  }
                }
                """;

        Movie detail = client.parseMovieDetail(mockDetailJson);
        assertNotNull(detail);
        assertEquals("Alfred Hitchcock", detail.director(), "Should accurately extract Director from crew");
        assertEquals(2, detail.cast().size(), "Should extract cast members");
        assertEquals("James Stewart", detail.cast().get(0));
        assertEquals("Kim Novak", detail.cast().get(1));
        assertEquals(128, detail.durationMinutes());
        assertEquals("2h 8m", detail.getFormattedDuration());
        assertEquals("tt0052357", detail.imdbId());
        assertEquals("https://www.imdb.com/title/tt0052357", detail.getImdbUrl());
        assertEquals("★ 8.2 (TMDB • 4.1k votes)", detail.getTmdbRatingFormatted());
    }
}
