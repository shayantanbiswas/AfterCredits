package com.aftercredits.ui.views;

import com.aftercredits.model.Movie;
import com.aftercredits.model.UserProfile;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.HorizontalMovieRow;
import com.aftercredits.ui.util.AsyncImageLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * The redesigned dashboard screen for AfterCredits.
 * Features a cinematic full-width hero banner and horizontally scrolling movie rows.
 */
public class HomeView extends JPanel {

    private final CinemaRepository repository;
    private final java.util.function.Consumer<Movie> onMovieSelected;

    public HomeView(CinemaRepository repository, java.util.function.Consumer<Movie> onMovieSelected) {
        this.repository = repository;
        this.onMovieSelected = onMovieSelected;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        // Build the main scrollable content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ThemeManager.BG_PRIMARY);
        contentPanel.setBorder(ThemeManager.createViewPadding());

        // 1. Welcome Header & Pulse Metrics
        contentPanel.add(createHeaderSection());
        contentPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));

        // 2. Cinematic Hero Banner
        contentPanel.add(new HeroBannerPanel(repository.getSpotlightMovie()));
        contentPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XXL));

        // 3. Cinematic movie catalogue rails
        addMovieRow(contentPanel, "Trending Now", repository.getTrendingMovies());
        addMovieRow(contentPanel, "Popular This Week", repository.getPopularMovies());
        addMovieRow(contentPanel, "Top Rated", repository.getTopRatedMovies());
        addMovieRow(contentPanel, "Now Playing", repository.getNowPlayingMovies());
        addMovieRow(contentPanel, "Coming Soon", repository.getUpcomingMovies());

        // 4. Personal rail
        addMovieRow(contentPanel, "From Your Watchlist", repository.getWatchlist());

        // Wrap in a styled scroll pane
        JScrollPane scrollPane = ThemeManager.createStyledScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addMovieRow(JPanel contentPanel, String title, java.util.List<Movie> movies) {
        if (movies == null || movies.isEmpty()) {
            return;
        }

        contentPanel.add(new HorizontalMovieRow(title, movies, onMovieSelected));
        contentPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XXL));
    }

    private JPanel createHeaderSection() {
        UserProfile profile = repository.getUserProfile();

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Greeting and subtitle
        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + profile.username() + " 👋");
        welcomeLabel.setFont(ThemeManager.FONT_PAGE_TITLE);
        welcomeLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Here is your cinema pulse and trending community favorites this week.");
        subtitleLabel.setFont(ThemeManager.FONT_BODY);
        subtitleLabel.setForeground(ThemeManager.TEXT_MUTED);

        titlesPanel.add(welcomeLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        // Quick status pills
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statsPanel.setOpaque(false);

        BadgeLabel loggedBadge = BadgeLabel.createAccentBadge(profile.filmsThisYear() + " Logged This Year");
        BadgeLabel identityBadge = new BadgeLabel("Identity: " + profile.cinemaIdentity());
        statsPanel.add(loggedBadge);
        statsPanel.add(identityBadge);

        header.add(titlesPanel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Custom component for the spotlight movie featuring a full-bleed backdrop image
     * and a gradient overlay for text readability.
     */
    private static class HeroBannerPanel extends JPanel {
        private Image backdropImage;

        public HeroBannerPanel(Movie movie) {
            if (movie == null) {
                JLabel unavailable = new JLabel(
                        "<html><div style='text-align:center'>" +
                        "<b>Featured spotlight unavailable</b><br>" +
                        "TMDB could not be reached right now. Please try again shortly." +
                        "</div></html>",
                        javax.swing.SwingConstants.CENTER
                );
                unavailable.setForeground(ThemeManager.TEXT_MUTED);
                unavailable.setFont(ThemeManager.FONT_SECTION_HEADING);
                add(unavailable, BorderLayout.CENTER);
                return;
            }

            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(800, 380));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));
            
            // Info block positioned at the bottom left
            JPanel infoBlock = new JPanel();
            infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
            infoBlock.setOpaque(false);
            infoBlock.setBorder(BorderFactory.createEmptyBorder(0, 32, 32, 32));

            // Meta tags row
            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(LEFT_ALIGNMENT);
            tagRow.add(BadgeLabel.createAccentBadge("FEATURED SPOTLIGHT"));
            tagRow.add(BadgeLabel.createYearBadge(movie.year()));
            tagRow.add(BadgeLabel.createGenreBadge(movie.getFormattedDuration()));
            tagRow.add(BadgeLabel.createRatingBadge(movie.rating()));

            // Cinematic Title
            JLabel titleLabel = new JLabel(movie.title());
            titleLabel.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(42f));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);

            // Tagline or Synopsis snippet
            JLabel taglineLabel = new JLabel(movie.tagline() != null ? movie.tagline() : "Directed by " + movie.director());
            taglineLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
            taglineLabel.setForeground(ThemeManager.ACCENT_GOLD);
            taglineLabel.setAlignmentX(LEFT_ALIGNMENT);

            // Call to Action Buttons
            JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            actionRow.setOpaque(false);
            actionRow.setAlignmentX(LEFT_ALIGNMENT);
            actionRow.add(ThemeManager.createAccentButton("Play Trailer"));
            actionRow.add(ThemeManager.createGhostButton("+ Watchlist"));

            // Assemble info block
            infoBlock.add(tagRow);
            infoBlock.add(Box.createVerticalStrut(12));
            infoBlock.add(titleLabel);
            infoBlock.add(Box.createVerticalStrut(6));
            infoBlock.add(taglineLabel);
            infoBlock.add(Box.createVerticalStrut(24));
            infoBlock.add(actionRow);

            add(infoBlock, BorderLayout.SOUTH);

            // Asynchronously load the high-res backdrop
            AsyncImageLoader.loadImage(
                    movie.getBackdropUrl("w1280"),
                    1200, 500, // Request large size for crisp rendering
                    movie.title(),
                    icon -> {
                        if (icon instanceof ImageIcon) {
                            this.backdropImage = ((ImageIcon) icon).getImage();
                            repaint();
                        }
                    }
            );
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Clip bounds for rounded corners
            RoundRectangle2D.Float clipShape = new RoundRectangle2D.Float(0, 0, w, h, ThemeManager.CARD_ARC, ThemeManager.CARD_ARC);
            g2.setClip(clipShape);

            // 2. Draw backdrop image or fallback solid color
            if (backdropImage != null) {
                // Scaled draw to fill the component
                g2.drawImage(backdropImage, 0, 0, w, h, this);
            } else {
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRect(0, 0, w, h);
            }

            // 3. Draw gradient overlay to ensure text readability
            // Gradient from fully transparent top to dark primary background at the bottom
            GradientPaint gradient = new GradientPaint(
                    0, h * 0.2f, new Color(15, 18, 24, 0),
                    0, h, new Color(15, 18, 24, 255) // Match ThemeManager.BG_PRIMARY
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, w, h);

            // 4. Reset clip and draw subtle border
            g2.setClip(null);
            g2.setColor(ThemeManager.BORDER_COLOR);
            g2.drawRoundRect(0, 0, w - 1, h - 1, ThemeManager.CARD_ARC, ThemeManager.CARD_ARC);

            g2.dispose();
        }
    }
}
