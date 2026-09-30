package com.aftercredits.ui;

import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.Sidebar;
import com.aftercredits.ui.views.DiscoverView;
import com.aftercredits.ui.views.HomeView;
import com.aftercredits.ui.views.MyCinemaView;
import com.aftercredits.ui.views.ProfileView;

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

    public MainFrame(CinemaRepository repository) {
        super("AfterCredits — Social Cinema Platform");

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

        // Instantiate and register the four Tier 1 views sharing the single repository instance
        cardsPanel.add(new HomeView(repository), Sidebar.NAV_HOME);
        cardsPanel.add(new DiscoverView(repository), Sidebar.NAV_DISCOVER);
        cardsPanel.add(new MyCinemaView(repository), Sidebar.NAV_MY_CINEMA);
        cardsPanel.add(new ProfileView(repository), Sidebar.NAV_PROFILE);

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
     *
     * @param screenKey key matching the target view (e.g. Sidebar.NAV_HOME)
     */
    public void navigateToScreen(String screenKey) {
        cardLayout.show(cardsPanel, screenKey);
        sidebar.setActiveScreen(screenKey);
    }
}
