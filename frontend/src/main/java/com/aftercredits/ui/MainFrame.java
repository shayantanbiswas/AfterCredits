package com.aftercredits.ui;

import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.Sidebar;
import com.aftercredits.ui.views.DiscoverView;
import com.aftercredits.ui.views.FilmRoomView;
import com.aftercredits.ui.views.GroupsView;
import com.aftercredits.ui.views.HomeView;
import com.aftercredits.ui.views.MovieDetailView;
import com.aftercredits.ui.views.MyCinemaView;
import com.aftercredits.ui.views.ProfileView;
import com.aftercredits.ui.views.ReviewsView;
import com.aftercredits.ui.views.SettingsView;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * The main application window for AfterCredits.
 * Utilizes a BorderLayout with the Sidebar on WEST and a CardLayout container on CENTER,
 * providing responsive, zero-flicker view swapping.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel cardsPanel;
    private final Sidebar sidebar;
    private final CinemaRepository repository;
    private String lastScreenKey = Sidebar.NAV_HOME;

    public MainFrame(CinemaRepository repository) {
        super("AfterCredits — Social Cinema Platform");
        this.repository = repository;

        // Window configuration
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 700));
        setPreferredSize(new Dimension(1220, 800));
        getContentPane().setBackground(ThemeManager.BG_PRIMARY);
        setLayout(new BorderLayout());

        // 1. Central Content Area using CardLayout
        this.cardLayout = new CardLayout();
        this.cardsPanel = new JPanel(cardLayout);
        this.cardsPanel.setBackground(ThemeManager.BG_PRIMARY);

        // Instantiate the core views
        // Note: For HomeView, we need to pass a callback so clicking a movie opens details.
        // We'll update HomeView later if needed, but for Discover and MyCinema, we can pass it now.
        HomeView homeView = new HomeView(repository, this::openMovieDetails); 
        DiscoverView discoverView = new DiscoverView(repository, this::openMovieDetails);
        MyCinemaView myCinemaView = new MyCinemaView(repository, this::openMovieDetails);
        ProfileView profileView = new ProfileView(repository);
        
        ReviewsView reviewsView = new ReviewsView(repository);
        FilmRoomView filmRoomView = new FilmRoomView(repository);
        GroupsView groupsView = new GroupsView(repository);
        SettingsView settingsView = new SettingsView(repository);

        // Register main views
        cardsPanel.add(homeView, Sidebar.NAV_HOME);
        cardsPanel.add(discoverView, Sidebar.NAV_DISCOVER);
        cardsPanel.add(myCinemaView, Sidebar.NAV_MY_CINEMA); // Register only once

        // Map Community sections
        cardsPanel.add(filmRoomView, Sidebar.NAV_FILM_ROOM);
        cardsPanel.add(reviewsView, Sidebar.NAV_REVIEWS);
        cardsPanel.add(groupsView, Sidebar.NAV_GROUPS);

        // Map Account sections
        cardsPanel.add(profileView, Sidebar.NAV_PROFILE);
        cardsPanel.add(settingsView, Sidebar.NAV_SETTINGS);

        // 2. Left Navigation Sidebar
        this.sidebar = new Sidebar(repository, this::navigateToScreen);

        // Assemble root window
        add(sidebar, BorderLayout.WEST);
        add(cardsPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null); // Center window on screen
    }

    /**
     * Swaps the visible screen using CardLayout and updates sidebar selection styling.
     */
    public void navigateToScreen(String screenKey) {
        if (!screenKey.startsWith("DETAIL_")) {
            lastScreenKey = screenKey;
        }

        // Map MyCinema sub-keys to the actual registered card key
        String cardKey = screenKey;
        if (screenKey.equals(Sidebar.NAV_WATCHED) || 
            screenKey.equals(Sidebar.NAV_WATCHLIST) || 
            screenKey.equals(Sidebar.NAV_DIARY) || 
            screenKey.equals(Sidebar.NAV_COLLECTIONS)) {
            cardKey = Sidebar.NAV_MY_CINEMA;
        }

        cardLayout.show(cardsPanel, cardKey);
        sidebar.setActiveScreen(screenKey);
        
        // Sync MyCinema tabs if that view is active
        for (java.awt.Component comp : cardsPanel.getComponents()) {
            if (comp instanceof MyCinemaView myCinema && comp.isVisible()) {
                myCinema.selectTab(screenKey);
            }
        }
    }

    /**
     * Dynamically creates and navigates to a MovieDetailView.
     */
    public void openMovieDetails(Movie movie) {
        String detailKey = "DETAIL_" + movie.id();
        
        // Remove old if exists to refresh data
        for (java.awt.Component comp : cardsPanel.getComponents()) {
            if (comp instanceof MovieDetailView) {
                cardsPanel.remove(comp);
            }
        }
        
        MovieDetailView detailView = new MovieDetailView(movie, repository, () -> {
            navigateToScreen(lastScreenKey); // Go back
        });
        
        cardsPanel.add(detailView, detailKey);
        navigateToScreen(detailKey);
    }
}
