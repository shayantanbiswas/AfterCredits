package com.aftercredits.ui.components;

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
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Left navigation sidebar component for the AfterCredits application shell.
 * Redesigned to match the new cinematic visual style with expanded navigation sections.
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

    private final Consumer<String> onNavigateListener;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activeScreenKey = NAV_HOME;

    public Sidebar(CinemaRepository repository, Consumer<String> onNavigateListener) {
        this.onNavigateListener = onNavigateListener;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_SIDEBAR);
        setPreferredSize(new Dimension(ThemeManager.SIDEBAR_WIDTH, 700));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.DIVIDER_COLOR));
        setMinimumSize(new Dimension(210, 600));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER_COLOR));

        // 1. Top Brand Logo & Header
        add(createBrandHeader(), BorderLayout.NORTH);

        // 2. Middle Navigation Buttons
        add(ThemeManager.createStyledScrollPane(createNavMenu()), BorderLayout.CENTER);

        // 3. Bottom User Profile Snippet
        add(createUserFooter(repository.getUserProfile()), BorderLayout.SOUTH);

        // Set initial active button styling
        setActiveScreen(NAV_HOME);
    }

    private JPanel createBrandHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(28, 22, 22, 22));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(LEFT_ALIGNMENT);

        CinemaLogo logoIcon = new CinemaLogo(34);

        JLabel brandName = new JLabel("AFTERCREDITS");
        brandName.setFont(ThemeManager.FONT_APP_TITLE.deriveFont(21f));
        brandName.setForeground(ThemeManager.ACCENT_GOLD);

        logoRow.add(logoIcon);
        logoRow.add(brandName);

        JLabel subtitle = new JLabel("Social Cinema Platform");
        subtitle.setFont(ThemeManager.FONT_SECONDARY);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 2, 0, 0));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        header.add(logoRow);
        header.add(subtitle);

        return header;
    }

    private JPanel createNavMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(12, 14, 16, 14));

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

        menu.add(Box.createVerticalGlue());
        return menu;
    }

    private void addSectionHeader(JPanel container, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(ThemeManager.FONT_SECONDARY);
        sectionLabel.setForeground(ThemeManager.TEXT_MUTED);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(12, 10, 8, 0));
        container.add(sectionLabel);
    }

    private void addNavButton(JPanel container, String screenKey, String labelText) {
        JButton btn = new JButton(labelText);
        btn.setFont(ThemeManager.FONT_BUTTON);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));

        btn.addActionListener(e -> {
            setActiveScreen(screenKey);
            if (onNavigateListener != null) {
                onNavigateListener.accept(screenKey);
            }
        });

        // Hover effect for unselected buttons
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
        container.add(Box.createVerticalStrut(2)); // Slight gap between buttons
    }

    private JPanel createUserFooter(UserProfile profile) {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(ThemeManager.BG_INPUT);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
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

        JLabel usernameLabel = new JLabel(profile.username());
        usernameLabel.setFont(ThemeManager.FONT_BUTTON);
        usernameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel identityLabel = new JLabel(profile.cinemaIdentity());
        identityLabel.setFont(ThemeManager.FONT_SECONDARY);
        identityLabel.setForeground(ThemeManager.ACCENT_GOLD_MUTED);

        userDetails.add(usernameLabel);
        userDetails.add(Box.createVerticalStrut(2));
        userDetails.add(identityLabel);

        footer.add(avatar, BorderLayout.WEST);
        footer.add(userDetails, BorderLayout.CENTER);

        return footer;
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
                        BorderFactory.createEmptyBorder(8, 11, 8, 14)
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
