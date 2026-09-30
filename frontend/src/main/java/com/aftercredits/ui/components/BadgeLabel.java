package com.aftercredits.ui.components;

import com.aftercredits.theme.ThemeManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Reusable rounded pill/badge label for genre tags, status indicators, and spoiler alerts.
 * Uses Graphics2D anti-aliasing to draw smooth rounded pill backgrounds.
 */
public class BadgeLabel extends JLabel {

    private Color badgeBackground;
    private Color badgeBorderColor;
    private final int cornerRadius;

    /**
     * Creates a standard badge with default muted styling.
     */
    public BadgeLabel(String text) {
        this(text, ThemeManager.BADGE_BG, ThemeManager.TEXT_MUTED, ThemeManager.BORDER_COLOR);
    }

    /**
     * Creates a badge with custom background and text color.
     */
    public BadgeLabel(String text, Color background, Color foreground) {
        this(text, background, foreground, null);
    }

    /**
     * Full constructor for complete badge styling.
     */
    public BadgeLabel(String text, Color background, Color foreground, Color borderColor) {
        super(text);
        this.badgeBackground = background;
        this.badgeBorderColor = borderColor;
        this.cornerRadius = 10;

        setForeground(foreground);
        setFont(ThemeManager.FONT_SMALL);
        setOpaque(false); // Required so our custom rounded background paints correctly
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
    }

    // --- Preset Factory Methods for Consistent UI Styling ---

    public static BadgeLabel createGenreBadge(String genre) {
        return new BadgeLabel(genre, ThemeManager.BADGE_BG, ThemeManager.TEXT_MUTED, ThemeManager.BORDER_COLOR);
    }

    public static BadgeLabel createAccentBadge(String text) {
        return new BadgeLabel(text, new Color(48, 38, 20), ThemeManager.ACCENT_GOLD, ThemeManager.ACCENT_GOLD_MUTED);
    }

    public static BadgeLabel createSpoilerBadge() {
        return new BadgeLabel("SPOILER", new Color(60, 22, 22), ThemeManager.SPOILER_ALERT, ThemeManager.SPOILER_ALERT);
    }

    public void setBadgeColors(Color background, Color foreground, Color border) {
        this.badgeBackground = background;
        this.badgeBorderColor = border;
        setForeground(foreground);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Enable anti-aliasing for smooth rounded corners
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 1. Draw rounded background pill
        if (badgeBackground != null) {
            g2.setColor(badgeBackground);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
        }

        // 2. Draw border outline if configured
        if (badgeBorderColor != null) {
            g2.setColor(badgeBorderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();

        // 3. Delegate text rendering to standard JLabel
        super.paintComponent(g);
    }
}
