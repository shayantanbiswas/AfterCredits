package com.aftercredits.theme;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

/**
 * Centralized theme configuration for the AfterCredits desktop application.
 * Manages color palette, standard typography, and FlatLaf dark look-and-feel initialization.
 */
public final class ThemeManager {

    // --- Color Palette: Cinematic Dark Theme ---
    public static final Color BG_PRIMARY = new Color(15, 18, 24);         // Deep Midnight Slate (Canvas)
    public static final Color BG_SIDEBAR = new Color(11, 14, 18);         // Darker Midnight (Sidebar)
    public static final Color BG_CARD = new Color(24, 29, 38);            // Surface Container for Cards
    public static final Color BG_CARD_HOVER = new Color(34, 41, 54);      // Hover state for interactive cards
    public static final Color ACCENT_GOLD = new Color(229, 169, 60);       // Cinema Gold (Primary Brand Accent)
    public static final Color ACCENT_GOLD_MUTED = new Color(175, 125, 40); // Darker Gold for secondary highlights
    public static final Color TEXT_PRIMARY = new Color(240, 243, 246);    // Crisp white-slate text
    public static final Color TEXT_MUTED = new Color(140, 150, 165);      // Secondary subtitles, metadata, dates
    public static final Color BORDER_COLOR = new Color(38, 46, 60);       // Card and panel dividers
    public static final Color BADGE_BG = new Color(32, 40, 52);           // Pill/Badge container background
    public static final Color SPOILER_ALERT = new Color(220, 80, 70);      // Warning tag for movie spoilers

    // --- Standard Typography ---
    public static final Font FONT_TITLE_LARGE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    public static final Font FONT_BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private ThemeManager() {
        // Private constructor prevents instantiation of utility class
    }

    /**
     * Initializes the FlatLaf dark theme and configures global UI properties.
     * Must be called on or before the Event Dispatch Thread (EDT) before constructing any Swing frames.
     */
    public static void initializeTheme() {
        // Set FlatLaf Dark Look and Feel
        FlatDarkLaf.setup();

        // Configure modern UI design properties (rounded corners, clean scrollbars)
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 8);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.selectedBackground", BG_CARD);
        UIManager.put("Table.alternateRowColor", new Color(20, 24, 32));
    }

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
     * Helper to generate consistent empty padding borders across views.
     */
    public static Border createPadding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }
}
