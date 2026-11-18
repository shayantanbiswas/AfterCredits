package com.aftercredits;

import com.aftercredits.client.auth.SessionManager;
import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.repository.MockCinemaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthAndSpotlightTest {

    @BeforeEach
    @AfterEach
    void clearState() {
        SessionManager.clearSession();
    }

    @Test
    void testSessionManagerGuestLoginAndId() {
        String guestId = SessionManager.generateGuestId();
        assertNotNull(guestId);
        assertTrue(guestId.startsWith("GUEST-"), "Generated guest ID should start with GUEST-");

        SessionManager.Session session = new SessionManager.Session(
                -1L, "Guest", null, "token-123", true, guestId, SessionManager.AuthProvider.GUEST
        );
        SessionManager.setSession(session);

        assertTrue(SessionManager.isLoggedIn());
        assertTrue(SessionManager.isGuest());
        assertEquals("Guest", SessionManager.getSession().getDisplayName());
        assertEquals(guestId, SessionManager.getSession().getGuestId());
        assertEquals("Guest (" + guestId + ")", SessionManager.getSession().getFormattedDisplayName());
        assertEquals("Guest", System.getProperty("aftercredits.user.name"));
        assertEquals(guestId, System.getProperty("aftercredits.user.guest_id"));
    }

    @Test
    void testSessionManagerUserLogin() {
        SessionManager.Session session = new SessionManager.Session(
                42L, "CinemaBuff", "buff@cinema.com", "jwt-token", false, null, SessionManager.AuthProvider.LOCAL
        );
        SessionManager.setSession(session);

        assertTrue(SessionManager.isLoggedIn());
        assertFalse(SessionManager.isGuest());
        assertEquals("CinemaBuff", SessionManager.getSession().getDisplayName());
        assertEquals("buff@cinema.com", SessionManager.getSession().getEmail());
        assertEquals(SessionManager.AuthProvider.LOCAL, SessionManager.getSession().getProvider());
    }

    @Test
    void testSpotlightWatchlistOperations() {
        CinemaRepository repository = new MockCinemaRepository();
        Movie spotlight = repository.getSpotlightMovie();
        assertNotNull(spotlight, "Spotlight movie should be available");

        String movieId = spotlight.id();

        // Ensure we can add to watchlist
        repository.addToWatchlist(movieId);
        assertTrue(repository.isWatchlisted(movieId), "Spotlight movie should now be in watchlist");

        boolean inWatchlist = repository.getWatchlist().stream().anyMatch(m -> m.id().equals(movieId));
        assertTrue(inWatchlist, "Watchlist collection should contain spotlight movie");

        // Ensure we can remove from watchlist
        repository.removeFromWatchlist(movieId);
        assertFalse(repository.isWatchlisted(movieId), "Spotlight movie should no longer be in watchlist");
    }

    @Test
    void testMovieTrailerUrl() {
        Movie movie = new Movie(
                "test-1", 12345, "Inception", "Inception", 2010, "2010-07-16",
                "Christopher Nolan", java.util.List.of(), java.util.List.of("Sci-Fi"),
                8.8, 8.8, 30000, 148, "Dream inside dream", "Your mind is the scene of the crime",
                null, null, "tt1375666", true
        );

        String trailerUrl = movie.getTrailerUrl();
        assertNotNull(trailerUrl);
        assertTrue(trailerUrl.startsWith("https://www.youtube.com/results?search_query="));
        assertTrue(trailerUrl.contains("Inception"));
        assertTrue(trailerUrl.contains("2010"));
    }

    @Test
    void testTmdbTrailerParsing() {
        String json = """
        {
          "id": 550,
          "results": [
            {
              "iso_639_1": "en",
              "site": "Vimeo",
              "type": "Trailer",
              "key": "vimeo123",
              "official": true
            },
            {
              "iso_639_1": "en",
              "site": "YouTube",
              "type": "Behind the Scenes",
              "key": "bts123",
              "official": false
            },
            {
              "iso_639_1": "en",
              "site": "YouTube",
              "type": "Teaser",
              "key": "teaserKey123",
              "official": false
            },
            {
              "iso_639_1": "en",
              "site": "YouTube",
              "type": "Trailer",
              "key": "officialTrailerKey",
              "official": true
            }
          ]
        }
        """;

        String key = com.aftercredits.client.tmdb.TmdbApiClient.parseTrailerKey(json);
        assertEquals("officialTrailerKey", key, "Should prioritize official YouTube trailer");
    }

    @Test
    void testAuthenticationStrictVerification() throws Exception {
        com.aftercredits.client.auth.LocalAuthService authService =
                com.aftercredits.client.auth.LocalAuthService.getInstance();

        // 1. Valid default credentials must succeed
        var adminResult = authService.login("admin", "Password123");
        assertNotNull(adminResult);
        assertEquals("admin", adminResult.displayName());

        // 2. Email login with valid credentials must succeed
        var demoResult = authService.login("demo@aftercredits.com", "Password123");
        assertNotNull(demoResult);
        assertEquals("demo", demoResult.displayName());

        // 3. Valid username with WRONG password must fail
        assertThrows(
                com.aftercredits.client.auth.LocalAuthService.AuthException.class,
                () -> authService.login("admin", "WrongPassword!"),
                "Wrong password must be rejected"
        );

        // 4. Non-existent user must fail
        assertThrows(
                com.aftercredits.client.auth.LocalAuthService.AuthException.class,
                () -> authService.login("ghost_user_999", "Password123"),
                "Unregistered account must be rejected"
        );

        // 5. Registration with valid details must succeed
        String uniqueUser = "user_" + System.currentTimeMillis();
        String uniqueEmail = uniqueUser + "@testcinema.com";
        var regResult = authService.register(uniqueUser, uniqueEmail, "SuperSecret2026");
        assertNotNull(regResult);
        assertEquals(uniqueUser, regResult.displayName());

        // 6. Registered user logging in with correct password must succeed
        var loginRegistered = authService.login(uniqueUser, "SuperSecret2026");
        assertNotNull(loginRegistered);
        assertEquals(uniqueUser, loginRegistered.displayName());

        // 7. Registered user logging in with wrong password must fail
        assertThrows(
                com.aftercredits.client.auth.LocalAuthService.AuthException.class,
                () -> authService.login(uniqueUser, "IncorrectPassword"),
                "Registered user with wrong password must be rejected"
        );

        // 8. Duplicate registration must fail
        assertThrows(
                com.aftercredits.client.auth.LocalAuthService.AuthException.class,
                () -> authService.register("anotherName", uniqueEmail, "SuperSecret2026"),
                "Duplicate email registration must be rejected"
        );
    }

    @Test
    void testBrowserOAuthPayload() {
        var payload = new com.aftercredits.client.auth.BrowserOAuthService.OAuthPayload(
                "GOOGLE", "google_12345", "Test Google User", "testuser@gmail.com"
        );
        assertEquals("GOOGLE", payload.provider());
        assertEquals("google_12345", payload.providerId());
        assertEquals("Test Google User", payload.displayName());
        assertEquals("testuser@gmail.com", payload.email());

        // Ensure cancellation cleanly resets
        com.aftercredits.client.auth.BrowserOAuthService.cancelActiveFlow();
    }
}
