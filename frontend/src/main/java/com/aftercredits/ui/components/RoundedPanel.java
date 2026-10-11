package com.aftercredits.ui.components;

import com.aftercredits.theme.ThemeManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Reusable anti-aliased rounded panel with configurable corner radius, 
 * background color, and optional border.
 */
public class RoundedPanel extends JPanel {
    private int arc;
    private Color backgroundColor;
    private Color borderColor;

    public RoundedPanel(Color backgroundColor, int arc) {
        this(backgroundColor, arc, null);
    }

    public RoundedPanel(Color backgroundColor, int arc, Color borderColor) {
        this.backgroundColor = backgroundColor;
        this.arc = arc;
        this.borderColor = borderColor;
        setOpaque(false);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, width, height, arc, arc);
        }

        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
        }

        g2.dispose();
    }
}
