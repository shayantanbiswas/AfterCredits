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
 * Provides navigation buttons with active highlight states, brand logo,
 * and a bottom user profile snippet.
 */
public class Sidebar extends JPanel {

    public static final String NAV_HOME = "HOME";
    public static final String NAV_DISCOVER = "DISCOVER";
    public static final String NAV_MY_CINEMA = "MY_CINEMA";
    public static final String NAV_PROFILE = "PROFILE";

    private final Consumer<String> onNavigateListener;
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private String activeScreenKey = NAV_HOME;

    public Sidebar(CinemaRepository repository, Consumer<String> onNavigateListener) {
        this.onNavigateListener = onNavigateListener;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_SIDEBAR);
        setPreferredSize(new Dimension(230, 700));
        setMinimumSize(new Dimension(210, 600));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.BORDER_COLOR));

        // 1. Top Brand Logo & Header
        add(createBrandHeader(), BorderLayout.NORTH);

        // 2. Middle Navigation Buttons
        add(createNavMenu(), BorderLayout.CENTER);

        // 3. Bottom User Profile Snippet
        add(createUserFooter(repository.getUserProfile()), BorderLayout.SOUTH);

        // Set initial active button styling
        setActiveScreen(NAV_HOME);
    }

    private JPanel createBrandHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);

        JLabel logoIcon = new JLabel("🎬");
        logoIcon.setFont(ThemeManager.FONT_TITLE_LARGE);

        JLabel brandName = new JLabel("AFTERCREDITS");
        brandName.setFont(ThemeManager.FONT_TITLE);
        brandName.setForeground(ThemeManager.ACCENT_GOLD);

        logoRow.add(logoIcon);
        logoRow.add(brandName);

        JLabel subtitle = new JLabel("Social Cinema Platform");
        subtitle.setFont(ThemeManager.FONT_SMALL);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        subtitle.setBorder(BorderFactory.createEmptyBorder(2, 34, 0, 0));

        header.add(logoRow);
        header.add(subtitle);

        return header;
    }

    private JPanel createNavMenu() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel sectionLabel = new JLabel("MAIN MENU");
        sectionLabel.setFont(ThemeManager.FONT_SMALL);
        sectionLabel.setForeground(ThemeManager.TEXT_MUTED);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 0));
        menu.add(sectionLabel);

        // Add the four active navigation items
        addNavButton(menu, NAV_HOME, "🏠  Home");
        addNavButton(menu, NAV_DISCOVER, "🔍  Discover");
        addNavButton(menu, NAV_MY_CINEMA, "🎞️  My Cinema");
        addNavButton(menu, NAV_PROFILE, "👤  Profile & DNA");

        menu.add(Box.createVerticalGlue());
        return menu;
    }

    private void addNavButton(JPanel container, String screenKey, String labelText) {
        JButton btn = new JButton(labelText);
        btn.setFont(ThemeManager.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

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
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!screenKey.equals(activeScreenKey)) {
                    btn.setBackground(ThemeManager.BG_SIDEBAR);
                }
            }
        });

        navButtons.put(screenKey, btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    private JPanel createUserFooter(UserProfile profile) {
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(new Color(15, 19, 26));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel avatar = new JLabel("👤", JLabel.CENTER);
        avatar.setFont(ThemeManager.FONT_BODY_BOLD);
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(true);
        avatar.setBackground(ThemeManager.BG_CARD);
        avatar.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1, true));

        JPanel userDetails = new JPanel();
        userDetails.setLayout(new BoxLayout(userDetails, BoxLayout.Y_AXIS));
        userDetails.setOpaque(false);

        JLabel usernameLabel = new JLabel(profile.username());
        usernameLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        usernameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel identityLabel = new JLabel(profile.cinemaIdentity());
        identityLabel.setFont(ThemeManager.FONT_SMALL);
        identityLabel.setForeground(ThemeManager.ACCENT_GOLD);

        userDetails.add(usernameLabel);
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
                btn.setBackground(ThemeManager.BG_CARD);
                btn.setForeground(ThemeManager.ACCENT_GOLD);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, ThemeManager.ACCENT_GOLD),
                        BorderFactory.createEmptyBorder(8, 11, 8, 14)
                ));
            } else {
                btn.setBackground(ThemeManager.BG_SIDEBAR);
                btn.setForeground(ThemeManager.TEXT_MUTED);
                btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            }
        }
        repaint();
    }
}
