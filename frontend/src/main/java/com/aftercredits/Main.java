package com.aftercredits;

import com.aftercredits.client.tmdb.TmdbApiClient;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.repository.MockCinemaRepository;
import com.aftercredits.repository.TmdbCinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.MainFrame;

import javax.swing.SwingUtilities;

/**
 * Main application entry point for the AfterCredits desktop client.
 * Configures the FlatLaf theme, selects the data repository (TMDB v3 live or offline fallback),
 * and launches the MainFrame onto the Swing Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // 1. Initialize FlatLaf Dark theme globally before constructing Swing components
        ThemeManager.initializeTheme();

        // 2. Resolve TMDB v3 API Key securely from environment variable (never logged or printed)
        String tmdbApiKey = System.getenv("TMDB_V3_API_KEY");
        if (tmdbApiKey == null || tmdbApiKey.isBlank()) {
            // Backward-compatible fallback check
            tmdbApiKey = System.getenv("TMDB_API_KEY");
        }

        CinemaRepository repository;
        if (tmdbApiKey != null && !tmdbApiKey.isBlank()) {
            System.out.println("[AfterCredits] TMDB_V3_API_KEY detected. Initialising TMDB Live Cinema Repository (v3)...");
            TmdbApiClient apiClient = new TmdbApiClient(tmdbApiKey);
            repository = new TmdbCinemaRepository(apiClient);
        } else {
            System.out.println("[AfterCredits] TMDB_V3_API_KEY environment variable not set.");
            System.out.println("[AfterCredits] Starting in Offline Demo Mode with pre-seeded mock catalogue.");
            repository = new MockCinemaRepository();
        }

        final CinemaRepository activeRepository = repository;

        // 3. Launch application window safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(activeRepository);
            frame.setVisible(true);
        });
    }
}
