package com.aftercredits.ui.components;

import com.aftercredits.theme.ThemeManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom Swing component that visualizes a user's "Film DNA" taste metrics.
 * Displays each genre affinity as a labelled horizontal percentage bar rendered with Graphics2D.
 */
public class DnaBarChart extends JPanel {

    // Distinct cinematic color palette for genre DNA strands
    private static final Color[] STRAND_COLORS = {
            ThemeManager.ACCENT_GOLD,            // Primary affinity: Cinema Gold
            new Color(235, 135, 60),             // Secondary: Warm Amber
            new Color(85, 165, 220),             // Tertiary: Cyan Steel
            new Color(165, 115, 215),            // Quaternary: Violet
            new Color(110, 135, 165)             // Quinary: Muted Slate
    };

    private static final Color TRACK_BACKGROUND = new Color(32, 38, 50);

    private final Map<String, Integer> dnaData = new LinkedHashMap<>();

    public DnaBarChart(Map<String, Integer> initialData) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        if (initialData != null) {
            setDnaData(initialData);
        }
    }

    /**
     * Updates the chart data and rebuilds the visual bar rows.
     *
     * @param data map of Genre name to percentage (0 - 100)
     */
    public void setDnaData(Map<String, Integer> data) {
        this.dnaData.clear();
        if (data != null) {
            this.dnaData.putAll(data);
        }
        rebuildChart();
    }

    private void rebuildChart() {
        removeAll();

        int colorIndex = 0;
        for (Map.Entry<String, Integer> entry : dnaData.entrySet()) {
            String genre = entry.getKey();
            int percentage = Math.max(0, Math.min(100, entry.getValue()));
            Color strandColor = STRAND_COLORS[colorIndex % STRAND_COLORS.length];

            // 1. Header row: Genre name (left) and Percentage (right)
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setOpaque(false);

            JLabel genreLabel = new JLabel(genre);
            genreLabel.setFont(ThemeManager.FONT_BODY_BOLD);
            genreLabel.setForeground(ThemeManager.TEXT_PRIMARY);

            JLabel percentLabel = new JLabel(percentage + "%");
            percentLabel.setFont(ThemeManager.FONT_BODY_BOLD);
            percentLabel.setForeground(strandColor);

            headerPanel.add(genreLabel, BorderLayout.WEST);
            headerPanel.add(percentLabel, BorderLayout.EAST);

            // 2. Custom visual horizontal bar
            HorizontalBar bar = new HorizontalBar(percentage, strandColor);

            // Add row to main layout
            add(headerPanel);
            add(Box.createVerticalStrut(4));
            add(bar);
            add(Box.createVerticalStrut(12));

            colorIndex++;
        }

        revalidate();
        repaint();
    }

    /**
     * Lightweight custom component painting a smooth, anti-aliased horizontal progress bar.
     */
    private static class HorizontalBar extends JComponent {
        private final int percentage;
        private final Color fillColor;

        public HorizontalBar(int percentage, Color fillColor) {
            this.percentage = percentage;
            this.fillColor = fillColor;
            setPreferredSize(new Dimension(100, 8));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
            setMinimumSize(new Dimension(40, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int cornerArc = 6;

            // 1. Draw track background
            g2.setColor(TRACK_BACKGROUND);
            g2.fillRoundRect(0, 0, width, height, cornerArc, cornerArc);

            // 2. Draw filled proportional DNA strand
            int fillWidth = (int) Math.round((width * (percentage / 100.0)));
            if (fillWidth > 0) {
                g2.setColor(fillColor);
                g2.fillRoundRect(0, 0, Math.min(width, fillWidth), height, cornerArc, cornerArc);
            }

            g2.dispose();
        }
    }
}
