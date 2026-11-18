package com.aftercredits.theme;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
    private static String activeTheme = "Dark";


    // ═══════════════════════════════════════════════════════════════
    //  COLOR PALETTE — Cinematic Dark Theme
    // ═══════════════════════════════════════════════════════════════

    // --- Backgrounds ---
    public static Color BG_PRIMARY       = new Color(10, 12, 16);      // Deep Midnight Slate (Canvas)
    public static Color BG_SIDEBAR       = new Color(8, 10, 13);      // Darker Midnight (Sidebar)
    public static Color BG_CARD          = new Color(20, 23, 30);      // Surface Container for Cards
    public static Color BG_CARD_HOVER    = new Color(31, 36, 46);      // Hover state for interactive cards
    public static Color BG_INPUT         = new Color(17, 20, 26);      // Input field backgrounds
    public static Color BG_SELECTED      = new Color(34, 39, 50);      // Selected item background
    public static Color BG_OVERLAY       = new Color(0, 0, 0, 160);    // Semi-transparent overlay

    // --- Primary Accent: Cinema Gold ---
    public static Color ACCENT_GOLD       = new Color(229, 169, 60);   // Primary Brand Accent
    public static Color ACCENT_GOLD_MUTED = new Color(175, 125, 40);   // Secondary Gold highlights
    public static Color ACCENT_GOLD_BG    = new Color(48, 38, 20);     // Gold-tinted background for badges

    // --- Text ---
    public static Color TEXT_PRIMARY  = new Color(240, 243, 246);      // Crisp white-slate text
    public static Color TEXT_MUTED    = new Color(151, 160, 174);      // Secondary subtitles, metadata
    public static Color TEXT_FAINT    = new Color(105, 113, 126);       // Tertiary hints, disabled text

    // --- Borders & Dividers ---
    public static Color BORDER_COLOR  = new Color(32, 37, 48);         // Card and panel dividers
    public static Color DIVIDER_COLOR = new Color(24, 28, 37);         // Section dividers

    // --- Semantic Colors ---
    public static Color SUCCESS       = new Color(72, 187, 120);       // Subtle green
    public static Color WARNING       = new Color(236, 178, 46);       // Subtle amber
    public static Color ERROR         = new Color(220, 80, 70);        // Subtle red
    public static Color SPOILER_ALERT = ERROR;                          // Warning tag for movie spoilers

    // --- Component-Specific ---
    public static Color BADGE_BG      = new Color(27, 32, 41);         // Pill/Badge container background
    public static Color RATING_STAR   = ACCENT_GOLD;                    // Filled star color

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
    public static final int CARD_ARC       = 14;    // Rounded corner radius for cards
    public static final int BUTTON_ARC     = 10;     // Rounded corner radius for buttons
    public static final int SIDEBAR_WIDTH  = 240;   // Sidebar preferred width
    public static final int CARD_GAP       = 16;    // Gap between cards in grids

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
        // The bundled OTF files use a font format that this Java runtime
        // rejects. Use the native macOS-friendly sans-serif fallback instead.
        primaryFontFamily = Font.SANS_SERIF;
        interRegular = null;
        interMedium = null;
        interSemiBold = null;
        interBold = null;
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
    public static String getTheme() {
        return activeTheme;
    }

    public static void setTheme(String theme) {
        if (theme == null) return;
        if (!theme.equals("Dark") && !theme.equals("Dim") && !theme.equals("Light")) return;

        activeTheme = theme;

        if ("Light".equals(theme)) {
            BG_PRIMARY = new Color(245, 246, 248);
            BG_SIDEBAR = new Color(235, 237, 241);
            BG_CARD = new Color(255, 255, 255);
            BG_CARD_HOVER = new Color(239, 241, 245);
            BG_INPUT = new Color(250, 250, 252);
            BG_SELECTED = new Color(226, 230, 237);
            BG_OVERLAY = new Color(0, 0, 0, 100);
            TEXT_PRIMARY = new Color(28, 31, 38);
            TEXT_MUTED = new Color(90, 98, 110);
            TEXT_FAINT = new Color(130, 137, 148);
            BORDER_COLOR = new Color(215, 219, 226);
            DIVIDER_COLOR = new Color(225, 228, 233);
            BADGE_BG = new Color(235, 238, 243);
        } else if ("Dim".equals(theme)) {
            BG_PRIMARY = new Color(27, 29, 35);
            BG_SIDEBAR = new Color(22, 24, 29);
            BG_CARD = new Color(37, 40, 47);
            BG_CARD_HOVER = new Color(48, 52, 61);
            BG_INPUT = new Color(31, 34, 40);
            BG_SELECTED = new Color(53, 58, 68);
            BG_OVERLAY = new Color(0, 0, 0, 140);
            TEXT_PRIMARY = new Color(226, 229, 234);
            TEXT_MUTED = new Color(163, 170, 181);
            TEXT_FAINT = new Color(122, 130, 143);
            BORDER_COLOR = new Color(53, 58, 68);
            DIVIDER_COLOR = new Color(43, 47, 56);
            BADGE_BG = new Color(42, 46, 54);
        } else {
            BG_PRIMARY = new Color(10, 12, 16);
            BG_SIDEBAR = new Color(8, 10, 13);
            BG_CARD = new Color(20, 23, 30);
            BG_CARD_HOVER = new Color(31, 36, 46);
            BG_INPUT = new Color(17, 20, 26);
            BG_SELECTED = new Color(34, 39, 50);
            BG_OVERLAY = new Color(0, 0, 0, 160);
            TEXT_PRIMARY = new Color(240, 243, 246);
            TEXT_MUTED = new Color(151, 160, 174);
            TEXT_FAINT = new Color(105, 113, 126);
            BORDER_COLOR = new Color(32, 37, 48);
            DIVIDER_COLOR = new Color(24, 28, 37);
            BADGE_BG = new Color(27, 32, 41);
        }

        SPOILER_ALERT = ERROR;
        RATING_STAR = ACCENT_GOLD;
        initializeTheme();
    }

    public static void initializeTheme() {
        if ("Light".equals(activeTheme)) {
            FlatLightLaf.setup();
        } else if ("Dim".equals(activeTheme)) {
            FlatDarculaLaf.setup();
        } else {
            FlatDarkLaf.setup();
        }

        // Modern UI design properties (rounded corners, clean scrollbars)
        UIManager.put("Button.arc", BUTTON_ARC);
        UIManager.put("Component.arc", BUTTON_ARC);
        UIManager.put("TextComponent.arc", BUTTON_ARC);
        UIManager.put("ScrollBar.thumbArc", BUTTON_ARC);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.track", BG_PRIMARY);
        UIManager.put("ScrollBar.thumb", new Color(62, 70, 84));
        UIManager.put("TabbedPane.showTabSeparators", false); // Cleaner look without dividers
        UIManager.put("TabbedPane.selectedBackground", new Color(0, 0, 0, 0)); // Transparent to avoid box
        UIManager.put("TabbedPane.hoverColor", new Color(255, 255, 255, 10)); // Very faint hover, no solid box
        UIManager.put("TabbedPane.focusColor", new Color(0, 0, 0, 0)); // Remove focus rectangle
        UIManager.put("TabbedPane.tabInsets", new java.awt.Insets(8, 16, 8, 16));
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.underlineColor", ACCENT_GOLD);
        UIManager.put("TabbedPane.contentAreaColor", BG_PRIMARY);
        UIManager.put("Table.alternateRowColor", new Color(17, 20, 26));

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
     * Creates a consistently styled text field for dialogs and input forms.
     */
    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_BODY);
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_GOLD);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

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
     * Unit increment is set to 32 for a 60fps-feel without micro-lag.
     */
    public static javax.swing.JScrollPane createStyledScrollPane(java.awt.Component view) {
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(view);
        sp.setBorder(null);
        sp.setBackground(BG_PRIMARY);
        sp.getViewport().setBackground(BG_PRIMARY);
        // Smooth scrolling: 32px per click gives a natural, non-laggy feel
        sp.getVerticalScrollBar().setUnitIncrement(32);
        sp.getVerticalScrollBar().setBlockIncrement(160);
        sp.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Enable double-buffering on the viewport for tear-free rendering
        sp.getViewport().setScrollMode(javax.swing.JViewport.BLIT_SCROLL_MODE);
        return sp;
    }
}
