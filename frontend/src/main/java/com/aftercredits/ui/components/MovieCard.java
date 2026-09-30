package com.aftercredits.ui.components;

import com.aftercredits.model.Movie;
import com.aftercredits.theme.ThemeManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
 * Reusable visual card component representing a film.
 * Displays title, release year, director, genre badges, star score, and tagline.
 * Features a smooth cinema banner header and interactive hover effects.
 */
public class MovieCard extends JPanel {

    private final Movie movie;
    private final Consumer<Movie> onSelectCallback;
    private boolean isHovered = false;

    public MovieCard(Movie movie) {
        this(movie, null);
    }

    public MovieCard(Movie movie, Consumer<Movie> onSelectCallback) {
        this.movie = movie;
        this.onSelectCallback = onSelectCallback;

        setLayout(new BorderLayout(0, 8));
        setBackground(ThemeManager.BG_CARD);
        setBorder(ThemeManager.createCardBorder());
        setPreferredSize(new Dimension(220, 260));
        setMinimumSize(new Dimension(190, 240));

        // 1. Top Poster Placeholder Banner
        add(createPosterBanner(), BorderLayout.NORTH);

        // 2. Middle Content Panel (Title, Director, Rating, Tagline)
        add(createDetailsPanel(), BorderLayout.CENTER);

        // 3. Bottom Genre Badges
        add(createGenreTagsPanel(), BorderLayout.SOUTH);

        // Interactive hover & click behavior
        setupInteractions();
    }

    private JPanel createPosterBanner() {
        // Custom panel painting a stylized cinema slate placeholder
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep cinematic gradient fill
                g2.setColor(new Color(20, 25, 34));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Subtle projector light accent line
                g2.setColor(new Color(45, 55, 72));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        banner.setLayout(new BorderLayout());
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(200, 75));
        banner.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Clapper / Film icon and Year
        JLabel iconLabel = new JLabel("🎬");
        iconLabel.setFont(ThemeManager.FONT_TITLE_LARGE);

        BadgeLabel yearBadge = new BadgeLabel(String.valueOf(movie.year()));
        BadgeLabel durationBadge = new BadgeLabel(movie.getFormattedDuration());

        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        badgesPanel.setOpaque(false);
        badgesPanel.add(yearBadge);
        badgesPanel.add(durationBadge);

        banner.add(iconLabel, BorderLayout.WEST);
        banner.add(badgesPanel, BorderLayout.EAST);

        return banner;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Movie Title
        JLabel titleLabel = new JLabel(movie.title());
        titleLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Director
        JLabel directorLabel = new JLabel("Dir. " + movie.director());
        directorLabel.setFont(ThemeManager.FONT_SMALL);
        directorLabel.setForeground(ThemeManager.TEXT_MUTED);
        directorLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Star Rating (Normalized 5-star display)
        StarRatingBar ratingBar = new StarRatingBar(movie.rating());
        ratingBar.setAlignmentX(LEFT_ALIGNMENT);

        // Tagline / Synopsis preview
        String previewText = movie.tagline().isEmpty() ? movie.synopsis() : movie.tagline();
        JLabel taglineLabel = new JLabel("<html><i>\"" + escapeHtml(previewText) + "\"</i></html>");
        taglineLabel.setFont(ThemeManager.FONT_SMALL);
        taglineLabel.setForeground(ThemeManager.TEXT_MUTED);
        taglineLabel.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(directorLabel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(ratingBar);
        panel.add(Box.createVerticalStrut(6));
        panel.add(taglineLabel);

        return panel;
    }

    private JPanel createGenreTagsPanel() {
        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tagsPanel.setOpaque(false);

        // Display up to 2 genre tags to maintain clean card margins
        int count = 0;
        for (String genre : movie.genres()) {
            if (count++ >= 2) break;
            tagsPanel.add(BadgeLabel.createGenreBadge(genre));
        }

        return tagsPanel;
    }

    private void setupInteractions() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                setBackground(ThemeManager.BG_CARD_HOVER);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                setBackground(ThemeManager.BG_CARD);
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

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public Movie getMovie() {
        return movie;
    }
}
