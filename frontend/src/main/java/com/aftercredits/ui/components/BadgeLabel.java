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

    public BadgeLabel(String text) {
        this(text, ThemeManager.BADGE_BG, ThemeManager.TEXT_MUTED, ThemeManager.BORDER_COLOR);
    }

    public BadgeLabel(String text, Color background, Color foreground) {
        this(text, background, foreground, null);
    }

    public BadgeLabel(String text, Color background, Color foreground, Color borderColor) {
        super(text);
        this.badgeBackground = background;
        this.badgeBorderColor = borderColor;
        this.cornerRadius = 12; // Increased from 10 to match new design system

        setForeground(foreground);
        setFont(ThemeManager.FONT_SECONDARY); // Use new hierarchy
        setOpaque(false); 
        setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
    }

    // --- Preset Factory Methods for Consistent UI Styling ---

    public static BadgeLabel createGenreBadge(String genre) {
        return new BadgeLabel(genre, ThemeManager.BADGE_BG, ThemeManager.TEXT_MUTED, ThemeManager.BORDER_COLOR);
    }

    public static BadgeLabel createAccentBadge(String text) {
        return new BadgeLabel(text, ThemeManager.ACCENT_GOLD_BG, ThemeManager.ACCENT_GOLD, ThemeManager.ACCENT_GOLD_MUTED);
    }

    public static BadgeLabel createSpoilerBadge() {
        return new BadgeLabel("SPOILER", new Color(60, 22, 22), ThemeManager.SPOILER_ALERT, ThemeManager.SPOILER_ALERT);
    }

    public static BadgeLabel createRatingBadge(double rating) {
        return new BadgeLabel(String.format("★ %.1f", rating), ThemeManager.BG_OVERLAY, ThemeManager.ACCENT_GOLD, null);
    }

    public static BadgeLabel createYearBadge(int year) {
        return new BadgeLabel(String.valueOf(year), ThemeManager.BADGE_BG, ThemeManager.TEXT_PRIMARY, ThemeManager.BORDER_COLOR);
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        if (badgeBackground != null) {
            g2.setColor(badgeBackground);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);
        }

        if (badgeBorderColor != null) {
            g2.setColor(badgeBorderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
