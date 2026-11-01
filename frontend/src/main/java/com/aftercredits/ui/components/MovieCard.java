package com.aftercredits.ui.components;

import com.aftercredits.model.Movie;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.util.AsyncImageLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Redesigned visual card component representing a film.
 * Poster-dominant layout using AsyncImageLoader.
 */
public class MovieCard extends JPanel {

    private final Movie movie;
    private final Consumer<Movie> onSelectCallback;
    private boolean isHovered = false;
    private JLabel posterLabel;

    public MovieCard(Movie movie) {
        this(movie, null);
    }

    public MovieCard(Movie movie, Consumer<Movie> onSelectCallback) {
        this.movie = movie;
        this.onSelectCallback = onSelectCallback;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY); 
        setPreferredSize(new Dimension(204, 332));
        setMinimumSize(new Dimension(180, 290));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // Tight cinematic card framing

        // 1. Top Poster Area (2:3 aspect ratio)
        add(createPosterContainer(), BorderLayout.CENTER);

        // 2. Bottom Details Area
        add(createDetailsPanel(), BorderLayout.SOUTH);

        setupInteractions();
        installClickForwarders(this);
    }

    private void installClickForwarders(java.awt.Component component) {
        if (component == this) {
            // The outer card already has its click listener.
        } else {
            component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onSelectCallback != null) {
                        onSelectCallback.accept(movie);
                    }
                }
            });
        }

        if (component instanceof java.awt.Container container) {
            for (java.awt.Component child : container.getComponents()) {
                installClickForwarders(child);
            }
        }
    }

    private JPanel createPosterContainer() {
        // Container to hold the poster and its overlay
        RoundedPanel container = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        container.setLayout(new BorderLayout());
        container.setPreferredSize(new Dimension(196, 274));

        posterLabel = new JLabel();
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setVerticalAlignment(SwingConstants.CENTER);
        posterLabel.setLayout(new BorderLayout());

        // Overlay Rating Badge on bottom-left of the poster
        JPanel overlayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 7));
        overlayPanel.setOpaque(false);
        if (movie.rating() > 0) {
            overlayPanel.add(BadgeLabel.createRatingBadge(movie.rating()));
        }
        posterLabel.add(overlayPanel, BorderLayout.SOUTH);

        container.add(posterLabel, BorderLayout.CENTER);

        // Load image using the AsyncImageLoader
        AsyncImageLoader.loadImage(
                movie.getPosterUrl("w342"),
                196, 274, 
                movie.title(),
                icon -> {
                    posterLabel.setIcon(icon);
                    posterLabel.repaint();
                }
        );

        return container;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(9, 2, 0, 2));

        // Movie Title (Single line, truncated with ellipsis if too long)
        JLabel titleLabel = new JLabel(movie.title());
        titleLabel.setFont(ThemeManager.FONT_MOVIE_TITLE.deriveFont(14.5f));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Metadata: Year • Runtime
        String metaText = movie.year() + " • " + movie.getFormattedDuration();
        JLabel metaLabel = new JLabel(metaText);
        metaLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(11.5f));
        metaLabel.setForeground(ThemeManager.TEXT_MUTED);
        metaLabel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(metaLabel);

        return panel;
    }

    private void setupInteractions() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onSelectCallback != null) {
                    onSelectCallback.accept(movie);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw subtle hover highlight behind the entire card
        if (isHovered) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ThemeManager.BG_CARD_HOVER);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeManager.CARD_ARC + 4, ThemeManager.CARD_ARC + 4);
            g2.dispose();
        }
    }

    public Movie getMovie() {
        return movie;
    }
}
