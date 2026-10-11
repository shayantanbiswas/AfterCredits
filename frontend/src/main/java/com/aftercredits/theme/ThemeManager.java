package com.aftercredits.theme;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

/**
 * Centralized design system for the AfterCredits desktop application.
 * Manages the complete visual language: color palette, typography hierarchy (Inter font family),
 * spacing constants, FlatLaf dark theme initialization, and reusable component factories.
 *
 * All UI components should reference these constants instead of hardcoding values.
 */
public final class ThemeManager {

    // ═══════════════════════════════════════════════════════════════
    //  COLOR PALETTE — Cinematic Dark Theme
    // ═══════════════════════════════════════════════════════════════

    // --- Backgrounds ---
    public static final Color BG_PRIMARY       = new Color(15, 18, 24);      // Deep Midnight Slate (Canvas)
    public static final Color BG_SIDEBAR       = new Color(11, 14, 18);      // Darker Midnight (Sidebar)
    public static final Color BG_CARD          = new Color(24, 29, 38);      // Surface Container for Cards
    public static final Color BG_CARD_HOVER    = new Color(34, 41, 54);      // Hover state for interactive cards
    public static final Color BG_INPUT         = new Color(20, 24, 32);      // Input field backgrounds
    public static final Color BG_SELECTED      = new Color(38, 44, 58);      // Selected item background
    public static final Color BG_OVERLAY       = new Color(0, 0, 0, 160);    // Semi-transparent overlay

    // --- Primary Accent: Cinema Gold ---
    public static final Color ACCENT_GOLD       = new Color(229, 169, 60);   // Primary Brand Accent
    public static final Color ACCENT_GOLD_MUTED = new Color(175, 125, 40);   // Secondary Gold highlights
    public static final Color ACCENT_GOLD_BG    = new Color(48, 38, 20);     // Gold-tinted background for badges

    // --- Text ---
    public static final Color TEXT_PRIMARY  = new Color(240, 243, 246);      // Crisp white-slate text
    public static final Color TEXT_MUTED    = new Color(140, 150, 165);      // Secondary subtitles, metadata
    public static final Color TEXT_FAINT    = new Color(90, 100, 115);       // Tertiary hints, disabled text

    // --- Borders & Dividers ---
    public static final Color BORDER_COLOR  = new Color(38, 46, 60);         // Card and panel dividers
    public static final Color DIVIDER_COLOR = new Color(30, 36, 48);         // Section dividers

    // --- Semantic Colors ---
    public static final Color SUCCESS       = new Color(72, 187, 120);       // Subtle green
    public static final Color WARNING       = new Color(236, 178, 46);       // Subtle amber
    public static final Color ERROR         = new Color(220, 80, 70);        // Subtle red
    public static final Color SPOILER_ALERT = ERROR;                          // Warning tag for movie spoilers

    // --- Component-Specific ---
    public static final Color BADGE_BG      = new Color(32, 40, 52);         // Pill/Badge container background
    public static final Color RATING_STAR   = ACCENT_GOLD;                    // Filled star color

    // ═══════════════════════════════════════════════════════════════
    //  SPACING SYSTEM
    // ═══════════════════════════════════════════════════════════════

    public static final int SPACING_XS  = 4;
    public static final int SPACING_SM  = 8;
    public static final int SPACING_MD  = 14;
    public static final int SPACING_LG  = 20;
    public static final int SPACING_XL  = 28;
    public static final int SPACING_XXL = 36;

    // --- Component Geometry ---
    public static final int CARD_ARC       = 12;    // Rounded corner radius for cards
    public static final int BUTTON_ARC     = 8;     // Rounded corner radius for buttons
    public static final int SIDEBAR_WIDTH  = 240;   // Sidebar preferred width
    public static final int CARD_GAP       = 14;    // Gap between cards in grids

    // ═══════════════════════════════════════════════════════════════
    //  TYPOGRAPHY — Inter Font Family
    // ═══════════════════════════════════════════════════════════════

    private static String primaryFontFamily = Font.SANS_SERIF;
    private static Font interRegular;
    private static Font interMedium;
    private static Font interSemiBold;
    private static Font interBold;

    // --- Font Hierarchy (8 levels) ---
    public static Font FONT_APP_TITLE;        // App branding in sidebar (24 Bold)
    public static Font FONT_PAGE_TITLE;       // Page headings (26 Bold)
    public static Font FONT_SECTION_HEADING;  // Section titles (18 SemiBold)
    public static Font FONT_MOVIE_TITLE;      // Movie names on cards/banners (15 SemiBold)
    public static Font FONT_METADATA;         // Year, runtime, genre badges (12 Medium)
    public static Font FONT_BODY;             // Descriptions, reviews, bios (13 Regular)
    public static Font FONT_SECONDARY;        // Muted helper text (11 Regular)
    public static Font FONT_BUTTON;           // Button labels (13 SemiBold)

    // Legacy aliases — preserved for backward compatibility with existing code
    public static Font FONT_TITLE_LARGE;
    public static Font FONT_TITLE;
    public static Font FONT_BODY_BOLD;
    public static Font FONT_SMALL;

    static {
        loadInterFont();
        buildFontHierarchy();
    }

    private ThemeManager() {
        // Private constructor prevents instantiation of utility class
    }

    // ═══════════════════════════════════════════════════════════════
    //  FONT LOADING
    // ═══════════════════════════════════════════════════════════════

    /**
     * Attempts to load the bundled Inter font family from classpath resources.
     * Falls back to system "Segoe UI" (Windows) or default sans-serif if unavailable.
     */
    private static void loadInterFont() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        interRegular  = loadFontResource("/fonts/inter/Inter-Regular.otf", ge);
        interMedium   = loadFontResource("/fonts/inter/Inter-Medium.otf", ge);
        interSemiBold = loadFontResource("/fonts/inter/Inter-SemiBold.otf", ge);
        interBold     = loadFontResource("/fonts/inter/Inter-Bold.otf", ge);

        if (interRegular != null) {
            primaryFontFamily = interRegular.getFamily();
        } else {
            // Fallback chain: Segoe UI (Windows) → Liberation Sans → default sans-serif
            String[] fallbackFonts = {"Segoe UI", "Liberation Sans", "Helvetica Neue"};
            String[] available = ge.getAvailableFontFamilyNames();
            for (String candidate : fallbackFonts) {
                for (String installed : available) {
                    if (installed.equalsIgnoreCase(candidate)) {
                        primaryFontFamily = installed;
                        return;
                    }
                }
            }
        }
    }

    private static Font loadFontResource(String resourcePath, GraphicsEnvironment ge) {
        try (InputStream is = ThemeManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            ge.registerFont(font);
            return font;
        } catch (FontFormatException | IOException e) {
            System.err.println("[ThemeManager] Could not load font: " + resourcePath + " — " + e.getMessage());
            return null;
        }
    }

    /**
     * Builds the 8-level font hierarchy using loaded Inter fonts or system fallback.
     */
    private static void buildFontHierarchy() {
        // If Inter loaded successfully, derive from loaded Font objects for exact weight control
        if (interBold != null) {
            FONT_APP_TITLE       = interBold.deriveFont(24f);
            FONT_PAGE_TITLE      = interBold.deriveFont(26f);
        } else {
            FONT_APP_TITLE       = new Font(primaryFontFamily, Font.BOLD, 24);
            FONT_PAGE_TITLE      = new Font(primaryFontFamily, Font.BOLD, 26);
        }

        if (interSemiBold != null) {
            FONT_SECTION_HEADING = interSemiBold.deriveFont(18f);
            FONT_MOVIE_TITLE     = interSemiBold.deriveFont(15f);
            FONT_BUTTON          = interSemiBold.deriveFont(13f);
        } else {
            FONT_SECTION_HEADING = new Font(primaryFontFamily, Font.BOLD, 18);
            FONT_MOVIE_TITLE     = new Font(primaryFontFamily, Font.BOLD, 15);
            FONT_BUTTON          = new Font(primaryFontFamily, Font.BOLD, 13);
        }

        if (interMedium != null) {
            FONT_METADATA        = interMedium.deriveFont(12f);
        } else {
            FONT_METADATA        = new Font(primaryFontFamily, Font.PLAIN, 12);
        }

        if (interRegular != null) {
            FONT_BODY            = interRegular.deriveFont(13f);
            FONT_SECONDARY       = interRegular.deriveFont(11f);
        } else {
            FONT_BODY            = new Font(primaryFontFamily, Font.PLAIN, 13);
            FONT_SECONDARY       = new Font(primaryFontFamily, Font.PLAIN, 11);
        }

        // Legacy aliases — map old names to new hierarchy
        FONT_TITLE_LARGE = FONT_PAGE_TITLE;
        FONT_TITLE       = FONT_SECTION_HEADING;
        FONT_BODY_BOLD   = FONT_BUTTON;
        FONT_SMALL       = FONT_SECONDARY;
    }

    // ═══════════════════════════════════════════════════════════════
    //  THEME INITIALIZATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Initializes the FlatLaf dark theme and configures global UI properties.
     * Must be called on or before the EDT before constructing any Swing frames.
     */
    public static void initializeTheme() {
        FlatDarkLaf.setup();

        // Modern UI design properties (rounded corners, clean scrollbars)
        UIManager.put("Button.arc", BUTTON_ARC);
        UIManager.put("Component.arc", BUTTON_ARC);
        UIManager.put("TextComponent.arc", BUTTON_ARC);
        UIManager.put("ScrollBar.thumbArc", BUTTON_ARC);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.track", BG_PRIMARY);
        UIManager.put("ScrollBar.thumb", new Color(55, 65, 80));
        UIManager.put("TabbedPane.showTabSeparators", false); // Cleaner look without dividers
        UIManager.put("TabbedPane.selectedBackground", new Color(0, 0, 0, 0)); // Transparent to avoid box
        UIManager.put("TabbedPane.hoverColor", new Color(255, 255, 255, 10)); // Very faint hover, no solid box
        UIManager.put("TabbedPane.focusColor", new Color(0, 0, 0, 0)); // Remove focus rectangle
        UIManager.put("TabbedPane.tabInsets", new java.awt.Insets(8, 16, 8, 16));
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.underlineColor", ACCENT_GOLD);
        UIManager.put("TabbedPane.contentAreaColor", BG_PRIMARY);
        UIManager.put("Table.alternateRowColor", new Color(20, 24, 32));

        // Set default font for all components
        if (interRegular != null) {
            UIManager.put("defaultFont", interRegular.deriveFont(13f));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  BORDER FACTORIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Factory method creating a consistent rounded card border with internal padding.
     */
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );
    }

    /**
     * Creates a card border with custom internal padding.
     */
    public static Border createCardBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(top, left, bottom, right)
        );
    }

    /**
     * Helper to generate consistent empty padding borders across views.
     */
    public static Border createPadding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    /**
     * Standard view padding used by all Tier-1 views.
     */
    public static Border createViewPadding() {
        return BorderFactory.createEmptyBorder(SPACING_XL, SPACING_XL, SPACING_XL, SPACING_XL);
    }

    // ═══════════════════════════════════════════════════════════════
    //  COMPONENT FACTORIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Creates a rounded panel with anti-aliased corners and configurable background.
     */
    public static JPanel createRoundedPanel(Color background, int arc) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };
    }

    /**
     * Creates a primary accent button with Cinema Gold styling.
     */
    public static JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(new Color(20, 16, 8));
        btn.setBackground(ACCENT_GOLD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(SPACING_SM, SPACING_LG, SPACING_SM, SPACING_LG));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_GOLD.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT_GOLD);
            }
        });
        return btn;
    }

    /**
     * Creates a secondary outlined ghost button.
     */
    public static JButton createGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_CARD);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(SPACING_SM - 1, SPACING_LG - 1, SPACING_SM - 1, SPACING_LG - 1)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BG_CARD_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_CARD);
            }
        });
        return btn;
    }

    /**
     * Creates a section header with title, optional subtitle, and "See All ›" link.
     *
     * @param title    Section heading text
     * @param subtitle Optional subtitle (null to omit)
     * @return Panel containing the formatted section header
     */
    public static JPanel createSectionHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setLayout(new java.awt.BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SECTION_HEADING);
        titleLabel.setForeground(TEXT_PRIMARY);

        header.add(titleLabel, java.awt.BorderLayout.WEST);

        if (subtitle != null) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(FONT_SECONDARY);
            subtitleLabel.setForeground(ACCENT_GOLD);
            subtitleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            header.add(subtitleLabel, java.awt.BorderLayout.EAST);
        }

        return header;
    }

    /**
     * Creates a thin horizontal divider line.
     */
    public static JPanel createDivider() {
        JPanel divider = new JPanel();
        divider.setBackground(DIVIDER_COLOR);
        divider.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return divider;
    }

    /**
     * Creates a consistent empty state panel with icon, title, and hint text.
     */
    public static JPanel createEmptyState(String icon, String title, String hint) {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(60, 0, 60, 0));

        JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
        iconLabel.setFont(FONT_PAGE_TITLE.deriveFont(36f));
        iconLabel.setForeground(TEXT_FAINT);
        iconLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_BUTTON);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(FONT_SECONDARY);
        hintLabel.setForeground(TEXT_MUTED);
        hintLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(javax.swing.Box.createVerticalStrut(SPACING_SM));
        panel.add(titleLabel);
        panel.add(javax.swing.Box.createVerticalStrut(SPACING_XS));
        panel.add(hintLabel);

        return panel;
    }

    /**
     * Configures a JScrollPane with consistent styling — no border, themed background,
     * hidden horizontal scrollbar, smooth vertical scrolling, and mouse-wheel forwarding
     * so that wheel events over child components (cards, labels, panels) are not swallowed.
     */
    public static javax.swing.JScrollPane createStyledScrollPane(java.awt.Component view) {
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(view);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_PRIMARY);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Fix Priority 1: install wheel-event forwarder so clicks over child components scroll correctly
        com.aftercredits.ui.util.ScrollWheelForwarder.install(sp);
        return sp;
    }
}
