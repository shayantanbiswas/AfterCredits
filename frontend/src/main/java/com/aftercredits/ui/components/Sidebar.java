package com.aftercredits.ui.components;

import com.aftercredits.client.auth.SessionManager;
import com.aftercredits.model.UserProfile;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Left navigation sidebar component for the AfterCredits application shell.
 * Features Main navigation, My Cinema library, Community spaces, and in-app Account Authentication.
 */
public class Sidebar extends JPanel {

    // Main
    public static final String NAV_HOME = "HOME";
    public static final String NAV_DISCOVER = "DISCOVER";
    // Legacy support
    public static final String NAV_MY_CINEMA = "MY_CINEMA";
    // My Cinema
    public static final String NAV_WATCHED = "WATCHED";
    public static final String NAV_WATCHLIST = "WATCHLIST";
    public static final String NAV_DIARY = "DIARY";
    public static final String NAV_COLLECTIONS = "COLLECTIONS";
    // Community
    public static final String NAV_FILM_ROOM = "FILM_ROOM";
    public static final String NAV_REVIEWS = "REVIEWS";
    public static final String NAV_GROUPS = "GROUPS";
    // Account
    public static final String NAV_PROFILE = "PROFILE";
    public static final String NAV_SETTINGS = "SETTINGS";
    public static final String NAV_AUTH = "AUTH";

    private final CinemaRepository repository;
    private final Consumer<String> onNavigateListener;
    private final Runnable onAuthRequested;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activeScreenKey = NAV_HOME;

    private JPanel userFooterContainer;
    private JButton authNavButton;

    public Sidebar(CinemaRepository repository, Consumer<String> onNavigateListener) {
        this(repository, onNavigateListener, null);
    }

    public Sidebar(CinemaRepository repository, Consumer<String> onNavigateListener, Runnable onAuthRequested) {
        this.repository = repository;
        this.onNavigateListener = onNavigateListener;
        this.onAuthRequested = onAuthRequested;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_SIDEBAR);
        setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 700));
        setMinimumSize(new Dimension(210, 600));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER_COLOR));

        // 1. Top Brand Logo & Header
        add(createBrandHeader(), BorderLayout.NORTH);

        // 2. Middle Navigation Buttons
        add(ThemeManager.createStyledScrollPane(createNavMenu()), BorderLayout.CENTER);

        // 3. Bottom User Profile Snippet
        userFooterContainer = new JPanel(new BorderLayout());
        userFooterContainer.setOpaque(false);
        userFooterContainer.add(createUserFooter(repository.getUserProfile()), BorderLayout.CENTER);
        add(userFooterContainer, BorderLayout.SOUTH);

        // Set initial active button styling
        setActiveScreen(NAV_HOME);
    }

    private JPanel createBrandHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(32, 22, 26, 22));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(LEFT_ALIGNMENT);

        CinemaLogo logoIcon = new CinemaLogo(34);

        JLabel brandName = new JLabel("AFTERCREDITS");
        brandName.setFont(ThemeManager.FONT_APP_TITLE.deriveFont(Font.BOLD, 20f));
        brandName.setForeground(ThemeManager.ACCENT_GOLD);

        logoRow.add(logoIcon);
        logoRow.add(brandName);

        JLabel subtitle = new JLabel("Social Cinema Platform");
        subtitle.setFont(ThemeManager.FONT_SECONDARY);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(7, 2, 0, 0));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        header.add(logoRow);
        header.add(subtitle);

        return header;
    }

    private JPanel createNavMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(14, 14, 20, 14));

        addSectionHeader(menu, "MAIN");
        addNavButton(menu, NAV_HOME, "🏠  Home");
        addNavButton(menu, NAV_DISCOVER, "🔍  Discover");
        menu.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));

        addSectionHeader(menu, "MY CINEMA");
        addNavButton(menu, NAV_WATCHED, "✓  Watched");
        addNavButton(menu, NAV_WATCHLIST, "📋  Watchlist");
        addNavButton(menu, NAV_DIARY, "📔  Diary");
        addNavButton(menu, NAV_COLLECTIONS, "📁  Collections");
        menu.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));

        addSectionHeader(menu, "COMMUNITY");
        addNavButton(menu, NAV_FILM_ROOM, "💬  Film Room");
        addNavButton(menu, NAV_REVIEWS, "✍️  Reviews");
        addNavButton(menu, NAV_GROUPS, "👥  Groups");
        menu.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));

        addSectionHeader(menu, "ACCOUNT");
        addNavButton(menu, NAV_PROFILE, "👤  Profile & DNA");
        addNavButton(menu, NAV_SETTINGS, "⚙️  Settings");

        // Direct in-app Auth button
        authNavButton = createAuthButton();
        menu.add(authNavButton);

        menu.add(Box.createVerticalGlue());
        return menu;
    }

    private JButton createAuthButton() {
        boolean isGuest = SessionManager.isGuest();
        String label = isGuest ? "🔐  Sign In / Sign Up" : "🔄  Switch Account";
        JButton btn = new JButton(label);
        btn.setFont(ThemeManager.FONT_BUTTON);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.ACCENT_GOLD_MUTED, 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        btn.setBackground(new Color(24, 28, 38));
        btn.setForeground(ThemeManager.ACCENT_GOLD);

        btn.addActionListener(e -> {
            if (onAuthRequested != null) {
                onAuthRequested.run();
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(ThemeManager.BG_SELECTED);
                btn.setForeground(ThemeManager.ACCENT_GOLD.brighter());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(24, 28, 38));
                btn.setForeground(ThemeManager.ACCENT_GOLD);
            }
        });

        return btn;
    }

    private void addSectionHeader(JPanel container, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(ThemeManager.FONT_SECONDARY.deriveFont(Font.BOLD, 10f));
        sectionLabel.setForeground(ThemeManager.TEXT_MUTED);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(16, 11, 9, 0));
        container.add(sectionLabel);
    }

    private void addNavButton(JPanel container, String screenKey, String labelText) {
        JButton btn = new JButton(labelText);
        btn.setFont(ThemeManager.FONT_BUTTON);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setPreferredSize(new Dimension(200, 46));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(ThemeManager.BG_SIDEBAR);
        btn.setForeground(ThemeManager.TEXT_MUTED);

        btn.addActionListener(e -> {
            setActiveScreen(screenKey);
            if (onNavigateListener != null) {
                onNavigateListener.accept(screenKey);
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!screenKey.equals(activeScreenKey)) {
                    btn.setBackground(ThemeManager.BG_CARD_HOVER);
                    btn.setForeground(ThemeManager.TEXT_PRIMARY);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!screenKey.equals(activeScreenKey)) {
                    btn.setBackground(ThemeManager.BG_SIDEBAR);
                    btn.setForeground(ThemeManager.TEXT_MUTED);
                }
            }
        });

        navButtons.put(screenKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(2));
    }

    private JPanel createUserFooter(UserProfile profile) {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(ThemeManager.BG_INPUT);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 16, 15, 16)
        ));

        JLabel avatar = new JLabel("👤", JLabel.CENTER);
        avatar.setFont(ThemeManager.FONT_SECTION_HEADING);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(true);
        avatar.setBackground(ThemeManager.BG_CARD);
        avatar.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1, true));

        JPanel userDetails = new JPanel();
        userDetails.setLayout(new BoxLayout(userDetails, BoxLayout.Y_AXIS));
        userDetails.setOpaque(false);

        SessionManager.Session session = SessionManager.getSession();
        String nameText;
        String subtitleText;

        if (session != null && session.isGuest()) {
            nameText = "Guest";
            subtitleText = (session.getGuestId() != null) ? session.getGuestId() : "Guest User";
        } else if (session != null) {
            nameText = session.getDisplayName();
            subtitleText = session.getProvider() != null ? (session.getProvider() + " Account") : profile.cinemaIdentity();
        } else {
            nameText = profile.username();
            subtitleText = profile.cinemaIdentity();
        }

        JLabel usernameLabel = new JLabel(nameText);
        usernameLabel.setFont(ThemeManager.FONT_BUTTON);
        usernameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel identityLabel = new JLabel(subtitleText);
        identityLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(10f));
        identityLabel.setForeground(ThemeManager.ACCENT_GOLD_MUTED);

        userDetails.add(usernameLabel);
        userDetails.add(Box.createVerticalStrut(2));
        userDetails.add(identityLabel);

        footer.add(avatar, BorderLayout.WEST);
        footer.add(userDetails, BorderLayout.CENTER);

        if (session != null && session.isGuest()) {
            JButton signBtn = new JButton("Sign In");
            signBtn.setFont(ThemeManager.FONT_BUTTON.deriveFont(9f));
            signBtn.setForeground(Color.BLACK);
            signBtn.setBackground(ThemeManager.ACCENT_GOLD);
            signBtn.setFocusPainted(false);
            signBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            signBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            signBtn.addActionListener(e -> {
                if (onAuthRequested != null) onAuthRequested.run();
            });
            footer.add(signBtn, BorderLayout.EAST);
        }

        footer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footer.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (onAuthRequested != null) onAuthRequested.run();
            }
        });

        return footer;
    }

    /**
     * Refreshes the user profile footer and auth action button after sign-in state changes.
     */
    public void refreshUser() {
        if (userFooterContainer != null) {
            userFooterContainer.removeAll();
            userFooterContainer.add(createUserFooter(repository.getUserProfile()), BorderLayout.CENTER);
            userFooterContainer.revalidate();
            userFooterContainer.repaint();
        }

        if (authNavButton != null) {
            boolean isGuest = SessionManager.isGuest();
            authNavButton.setText(isGuest ? "🔐  Sign In / Sign Up" : "🔄  Switch Account");
            authNavButton.repaint();
        }
    }

    /**
     * Updates navigation button appearances to reflect the active screen.
     */
    public void setActiveScreen(String screenKey) {
        this.activeScreenKey = screenKey;

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            boolean isSelected = entry.getKey().equals(screenKey);

            if (isSelected) {
                btn.setBackground(ThemeManager.BG_SELECTED);
                btn.setForeground(ThemeManager.ACCENT_GOLD);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, ThemeManager.ACCENT_GOLD),
                        BorderFactory.createEmptyBorder(9, 11, 9, 14)
                ));
            } else {
                btn.setBackground(ThemeManager.BG_SIDEBAR);
                btn.setForeground(ThemeManager.TEXT_MUTED);
                btn.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
            }
        }
        repaint();
    }
}
