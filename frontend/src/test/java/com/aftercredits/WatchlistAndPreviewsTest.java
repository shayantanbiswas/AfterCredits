package com.aftercredits;

import com.aftercredits.client.tmdb.TmdbApiClient;
import com.aftercredits.model.CustomWatchlist;
import com.aftercredits.model.Movie;
import com.aftercredits.model.VideoClip;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.repository.MockCinemaRepository;
import com.aftercredits.repository.TmdbCinemaRepository;
import com.aftercredits.repository.WatchlistManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class WatchlistAndPreviewsTest {

    private CinemaRepository repository;
    private WatchlistManager watchlistManager;

    @BeforeEach
    void setUp() {
        watchlistManager = WatchlistManager.getInstance();
        repository = new MockCinemaRepository();
    }

    @Test
    @DisplayName("1. Movie previews and trailer clips are provided for movies")
    void testMoviePreviewsResolution() {
        Optional<Movie> movieOpt = repository.getMovieById("mov-1");
        assertTrue(movieOpt.isPresent());
        Movie movie = movieOpt.get();

        List<VideoClip> previews = repository.getMoviePreviews(movie);
        assertNotNull(previews);
        assertFalse(previews.isEmpty(), "Every movie must have previews/clips available");
        assertTrue(previews.size() >= 3, "Expected at least 3 preview clips");

        // Verify trailer and teaser clips
        boolean hasTrailer = previews.stream().anyMatch(c -> "Trailer".equalsIgnoreCase(c.type()));
        boolean hasTeaser = previews.stream().anyMatch(c -> "Teaser".equalsIgnoreCase(c.type()));
        assertTrue(hasTrailer, "Must include official trailer");
        assertTrue(hasTeaser, "Must include teaser preview");

        VideoClip first = previews.getFirst();
        assertNotNull(first.getPlayUrl(movie.title()));
        assertTrue(first.getPlayUrl(movie.title()).contains("youtube.com"));
        assertNotNull(first.getBadgeLabel());
    }

    @Test
    @DisplayName("2. TMDB Video JSON parsing extracts YouTube trailers and clips")
    void testTmdbVideoClipsParsing() {
        String tmdbJson = """
        {
          "id": 335984,
          "results": [
            {
              "iso_639_1": "en",
              "iso_3166_1": "US",
              "name": "Blade Runner 2049 - Official Trailer",
              "key": "gCcx85zbxz4",
              "site": "YouTube",
              "size": 1080,
              "type": "Trailer",
              "official": true,
              "published_at": "2017-05-08T16:00:01.000Z",
              "id": "5910901dc3a36864e404bfcb"
            },
            {
              "iso_639_1": "en",
              "iso_3166_1": "US",
              "name": "Blade Runner 2049 - Teaser Trailer",
              "key": "haXvp8M9FGg",
              "site": "YouTube",
              "size": 1080,
              "type": "Teaser",
              "official": true,
              "published_at": "2016-12-19T17:00:02.000Z",
              "id": "5857ff179251410b0a0026f8"
            }
          ]
        }
        """;

        List<VideoClip> clips = TmdbApiClient.parseVideoClips(tmdbJson);
        assertEquals(2, clips.size());
        assertEquals("gCcx85zbxz4", clips.get(0).key());
        assertEquals("https://www.youtube.com/watch?v=gCcx85zbxz4", clips.get(0).getPlayUrl("Blade Runner 2049"));
        assertEquals("https://img.youtube.com/vi/gCcx85zbxz4/hqdefault.jpg", clips.get(0).getThumbnailUrl());
        assertEquals("OFFICIAL TRAILER", clips.get(0).getBadgeLabel());

        assertEquals("TEASER", clips.get(1).getBadgeLabel());
    }

    @Test
    @DisplayName("3. Existing Add to Watchlist functionality works and updates state")
    void testDefaultWatchlistAddAndRemove() {
        String testMovieId = "mov-6"; // Whiplash
        assertFalse(repository.isWatchlisted(testMovieId));

        AtomicBoolean notified = new AtomicBoolean(false);
        watchlistManager.addChangeListener(() -> notified.set(true));

        repository.addToWatchlist(testMovieId);
        assertTrue(repository.isWatchlisted(testMovieId));
        assertTrue(repository.getWatchlist().stream().anyMatch(m -> m.id().equals(testMovieId)));
        assertTrue(notified.get(), "Watchlist listeners must be notified on add");

        notified.set(false);
        repository.removeFromWatchlist(testMovieId);
        assertFalse(repository.isWatchlisted(testMovieId));
        assertFalse(repository.getWatchlist().stream().anyMatch(m -> m.id().equals(testMovieId)));
        assertTrue(notified.get(), "Watchlist listeners must be notified on remove");
    }

    @Test
    @DisplayName("4. Custom Watchlists can be created with Private/Public options")
    void testCreateCustomWatchlist() {
        // Create Private Watchlist
        CustomWatchlist privateList = repository.createCustomWatchlist(
                "Noir Mysteries",
                "Deep detective and noir crime films",
                true // Private
        );

        assertNotNull(privateList.getId());
        assertEquals("Noir Mysteries", privateList.getName());
        assertTrue(privateList.isPrivate());
        assertEquals("🔒 Private", privateList.getPrivacyLabel());
        assertEquals(0, privateList.getMovieCount());

        // Create Public Watchlist
        CustomWatchlist publicList = repository.createCustomWatchlist(
                "Sci-Fi Odyssey",
                "Grand interstellar space journeys",
                false // Public
        );

        assertNotNull(publicList.getId());
        assertEquals("Sci-Fi Odyssey", publicList.getName());
        assertFalse(publicList.isPrivate());
        assertEquals("🌐 Public", publicList.getPrivacyLabel());

        List<CustomWatchlist> allLists = repository.getCustomWatchlists();
        assertTrue(allLists.stream().anyMatch(w -> w.getId().equals(privateList.getId())));
        assertTrue(allLists.stream().anyMatch(w -> w.getId().equals(publicList.getId())));
    }

    @Test
    @DisplayName("5. Movies can be added to and removed from existing Custom Watchlists")
    void testAddAndRemoveMoviesFromCustomWatchlist() {
        CustomWatchlist list = repository.createCustomWatchlist(
                "Anime Masterpieces",
                "Studio Ghibli and visionary animation",
                false
        );

        String spirittedAwayId = "mov-4"; // Spirited Away
        assertFalse(list.containsMovie(spirittedAwayId));

        // Add movie
        boolean added = repository.addMovieToCustomWatchlist(list.getId(), spirittedAwayId);
        assertTrue(added);

        CustomWatchlist updated = repository.getCustomWatchlist(list.getId()).orElseThrow();
        assertTrue(updated.containsMovie(spirittedAwayId));
        assertEquals(1, updated.getMovieCount());

        List<Movie> moviesInList = repository.getMoviesForCustomWatchlist(list.getId());
        assertEquals(1, moviesInList.size());
        assertEquals("Spirited Away", moviesInList.getFirst().title());

        // Verify membership query
        List<CustomWatchlist> containing = repository.getWatchlistsContainingMovie(spirittedAwayId);
        assertTrue(containing.stream().anyMatch(w -> w.getId().equals(list.getId())));

        // Remove movie
        boolean removed = repository.removeMovieFromCustomWatchlist(list.getId(), spirittedAwayId);
        assertTrue(removed);

        updated = repository.getCustomWatchlist(list.getId()).orElseThrow();
        assertFalse(updated.containsMovie(spirittedAwayId));
        assertEquals(0, updated.getMovieCount());
    }

    @Test
    @DisplayName("6. Private/Public privacy toggle works dynamically on Watchlists")
    void testToggleWatchlistPrivacy() {
        CustomWatchlist list = repository.createCustomWatchlist(
                "Secret Screenings",
                "Unpublished personal reviews and queue",
                true // Starts private
        );

        assertTrue(list.isPrivate());
        assertEquals("🔒 Private", list.getPrivacyLabel());

        // Toggle to public
        boolean updatedToPublic = repository.setCustomWatchlistPrivacy(list.getId(), false);
        assertTrue(updatedToPublic);

        CustomWatchlist refreshed = repository.getCustomWatchlist(list.getId()).orElseThrow();
        assertFalse(refreshed.isPrivate());
        assertEquals("🌐 Public", refreshed.getPrivacyLabel());

        // Toggle back to private
        boolean updatedToPrivate = repository.setCustomWatchlistPrivacy(list.getId(), true);
        assertTrue(updatedToPrivate);

        refreshed = repository.getCustomWatchlist(list.getId()).orElseThrow();
        assertTrue(refreshed.isPrivate());
        assertEquals("🔒 Private", refreshed.getPrivacyLabel());
    }

    @Test
    @DisplayName("7. TmdbCinemaRepository delegates and synchronizes Custom Watchlists")
    void testTmdbCinemaRepositoryCustomWatchlists() {
        TmdbCinemaRepository tmdbRepo = new TmdbCinemaRepository(new TmdbApiClient("mock-key"));

        CustomWatchlist wl = tmdbRepo.createCustomWatchlist(
                "Modern Auteurs",
                "Nolan, Villeneuve, Bong Joon-ho",
                false
        );

        assertNotNull(wl);
        tmdbRepo.addMovieToCustomWatchlist(wl.getId(), "mov-5"); // Dune: Part Two

        List<CustomWatchlist> lists = tmdbRepo.getCustomWatchlists();
        assertTrue(lists.stream().anyMatch(w -> w.getId().equals(wl.getId())));

        List<Movie> movies = tmdbRepo.getMoviesForCustomWatchlist(wl.getId());
        assertEquals(1, movies.size());
        assertEquals("Dune: Part Two", movies.getFirst().title());
    }
}

