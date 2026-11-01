package com.aftercredits.ui.components;

import com.aftercredits.theme.ThemeManager;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Reusable cinematic brand mark for AfterCredits.
 * Draws a gold film-frame emblem with an AC monogram.
 */
public class CinemaLogo extends JComponent {

    private final int logoSize;

    public CinemaLogo(int logoSize) {
        this.logoSize = Math.max(32, logoSize);
        setOpaque(false);
        setPreferredSize(new Dimension(this.logoSize, this.logoSize));
        setMinimumSize(new Dimension(this.logoSize, this.logoSize));
        setMaximumSize(new Dimension(this.logoSize, this.logoSize));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        int size = Math.min(getWidth(), getHeight());
        int inset = Math.max(2, size / 12);
        int frameWidth = size - inset * 2;
        int frameHeight = size - inset * 2;

        g2.setColor(ThemeManager.ACCENT_GOLD_BG);
        g2.fill(new RoundRectangle2D.Float(
                inset, inset, frameWidth, frameHeight,
                size * 0.18f, size * 0.18f
        ));

        g2.setColor(ThemeManager.ACCENT_GOLD);
        g2.setStroke(new BasicStroke(Math.max(1.4f, size / 28f)));
        g2.draw(new RoundRectangle2D.Float(
                inset, inset, frameWidth, frameHeight,
                size * 0.18f, size * 0.18f
        ));

        int perforationWidth = Math.max(2, size / 13);
        int perforationHeight = Math.max(3, size / 9);
        int perforationY = inset + Math.max(3, size / 10);

        g2.fillRoundRect(
                inset + Math.max(3, size / 10),
                perforationY,
                perforationWidth,
                perforationHeight,
                2, 2
        );
        g2.fillRoundRect(
                size - inset - Math.max(3, size / 10) - perforationWidth,
                perforationY,
                perforationWidth,
                perforationHeight,
                2, 2
        );

        String monogram = "AC";
        g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, size / 2)));
        FontMetrics metrics = g2.getFontMetrics();

        int textX = (size - metrics.stringWidth(monogram)) / 2;
        int textY = (size - metrics.getHeight()) / 2 + metrics.getAscent();

        g2.setColor(ThemeManager.ACCENT_GOLD);
        g2.drawString(monogram, textX, textY);

        g2.dispose();
    }
}
