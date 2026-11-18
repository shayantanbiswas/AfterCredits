package com.aftercredits.ui.views;

import com.aftercredits.client.auth.SessionManager;
import com.aftercredits.model.Movie;
import com.aftercredits.model.UserProfile;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.HorizontalMovieRow;
import com.aftercredits.ui.util.AsyncImageLoader;
import com.aftercredits.ui.util.TrailerPlayer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
 * Features a cinematic full-width hero banner with functional Spotlight trailer playback,
 * direct Watchlist management, and horizontally scrolling movie rows.
 */
public class HomeView extends JPanel {

    private final CinemaRepository repository;
    private final java.util.function.Consumer<Movie> onMovieSelected;

    private JPanel contentPanel;
    private JPanel watchlistRowContainer;
    private HeroBannerPanel heroBannerPanel;

    public HomeView(CinemaRepository repository, java.util.function.Consumer<Movie> onMovieSelected) {
        this.repository = repository;
        this.onMovieSelected = onMovieSelected;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        com.aftercredits.repository.WatchlistManager.getInstance().addChangeListener(() -> {
            javax.swing.SwingUtilities.invokeLater(this::refreshWatchlistRow);
        });

        buildContent();
    }

    private void buildContent() {
        removeAll();

        // Build the main scrollable content panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ThemeManager.BG_PRIMARY);
        contentPanel.setOpaque(true);
        contentPanel.setDoubleBuffered(true);
        contentPanel.setBorder(ThemeManager.createViewPadding());

        // 1. Welcome Header & Pulse Metrics
        contentPanel.add(createHeaderSection());
        contentPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));

        // 2. Cinematic Hero Banner (Spotlight)
        Movie spotlightMovie = repository.getSpotlightMovie();
        heroBannerPanel = new HeroBannerPanel(spotlightMovie, repository, onMovieSelected, this::refreshWatchlistRow);
        contentPanel.add(heroBannerPanel);
        contentPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XXL + 8));

        // 3. Cinematic movie catalogue rails
        addMovieRow(contentPanel, "Trending Now", repository.getTrendingMovies());
        addMovieRow(contentPanel, "Popular This Week", repository.getPopularMovies());
        addMovieRow(contentPanel, "Top Rated", repository.getTopRatedMovies());
        addMovieRow(contentPanel, "Now Playing", repository.getNowPlayingMovies());
        addMovieRow(contentPanel, "Coming Soon", repository.getUpcomingMovies());

        // 4. Personal rail: "From Your Watchlist"
        watchlistRowContainer = new JPanel();
        watchlistRowContainer.setLayout(new BoxLayout(watchlistRowContainer, BoxLayout.Y_AXIS));
        watchlistRowContainer.setOpaque(false);
        refreshWatchlistRow();
        contentPanel.add(watchlistRowContainer);

        // Wrap in a styled scroll pane
        JScrollPane scrollPane = ThemeManager.createStyledScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    /**
     * Refreshes the Watchlist row without reloading the whole screen.
     */
    public void refreshWatchlistRow() {
        if (watchlistRowContainer == null) return;

        watchlistRowContainer.removeAll();
        java.util.List<Movie> watchlist = repository.getWatchlist();
        if (watchlist != null && !watchlist.isEmpty()) {
            watchlistRowContainer.add(new HorizontalMovieRow("From Your Watchlist", watchlist, onMovieSelected));
            watchlistRowContainer.add(Box.createVerticalStrut(ThemeManager.SPACING_XXL));
        }
        watchlistRowContainer.revalidate();
        watchlistRowContainer.repaint();

        if (heroBannerPanel != null) {
            heroBannerPanel.syncWatchlistButton();
        }
    }

    /**
     * Fully refreshes the Home screen (greeting, watchlist, etc.).
     */
    public void refresh() {
        buildContent();
    }

    private void addMovieRow(JPanel container, String title, java.util.List<Movie> movies) {
        if (movies == null || movies.isEmpty()) {
            return;
        }

        container.add(new HorizontalMovieRow(title, movies, onMovieSelected));
        container.add(Box.createVerticalStrut(ThemeManager.SPACING_XXL));
    }

    private JPanel createHeaderSection() {
        UserProfile profile = repository.getUserProfile();
        SessionManager.Session session = SessionManager.getSession();

        String displayName;
        if (session != null) {
            displayName = session.getFormattedDisplayName();
        } else {
            displayName = profile.username();
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        // Greeting and subtitle
        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + displayName + " 👋");
        welcomeLabel.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(java.awt.Font.BOLD, 27f));
        welcomeLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Here is your cinema pulse and trending community favorites this week.");
        subtitleLabel.setFont(ThemeManager.FONT_BODY.deriveFont(13f));
        subtitleLabel.setForeground(ThemeManager.TEXT_MUTED);

        titlesPanel.add(welcomeLabel);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLabel);

        // Quick status pills
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statsPanel.setOpaque(false);

        String badgeText = (session != null && session.isGuest())
                ? "Guest Mode"
                : (profile.filmsThisYear() + " Logged This Year");

        BadgeLabel loggedBadge = BadgeLabel.createAccentBadge(badgeText);
        BadgeLabel identityBadge = new BadgeLabel("Identity: " + profile.cinemaIdentity());
        statsPanel.add(loggedBadge);
        statsPanel.add(identityBadge);

        header.add(titlesPanel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        return header;
    }

    /**
     * Custom component for the spotlight movie featuring a full-bleed backdrop image,
     * gradient overlay, trailer playback, and direct watchlist management.
     */
    private static class HeroBannerPanel extends JPanel {
        private final Movie movie;
        private final CinemaRepository repository;
        private final java.util.function.Consumer<Movie> onMovieSelected;
        private final Runnable onWatchlistChanged;

        private JButton watchlistBtn;
        private JButton playTrailerBtn;
        private Image backdropImage;

        public HeroBannerPanel(
                Movie movie,
                CinemaRepository repository,
                java.util.function.Consumer<Movie> onMovieSelected,
                Runnable onWatchlistChanged) {

            this.movie = movie;
            this.repository = repository;
            this.onMovieSelected = onMovieSelected;
            this.onWatchlistChanged = onWatchlistChanged;

            if (movie == null) {
                setLayout(new BorderLayout());
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
            setPreferredSize(new Dimension(800, 420));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));

            // Info block positioned at the bottom left
            JPanel infoBlock = new JPanel();
            infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
            infoBlock.setOpaque(false);
            infoBlock.setBorder(BorderFactory.createEmptyBorder(0, 38, 34, 38));

            // Meta tags row
            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(LEFT_ALIGNMENT);
            tagRow.add(BadgeLabel.createAccentBadge("FEATURED SPOTLIGHT"));
            if (movie.year() > 0) {
                tagRow.add(BadgeLabel.createYearBadge(movie.year()));
            }
            if (movie.durationMinutes() > 0) {
                tagRow.add(BadgeLabel.createGenreBadge(movie.getFormattedDuration()));
            }
            if (movie.rating() > 0) {
                tagRow.add(BadgeLabel.createRatingBadge(movie.rating()));
            }

            // Cinematic Title
            JLabel titleLabel = new JLabel(movie.title());
            titleLabel.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(java.awt.Font.BOLD, 44f));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setAlignmentX(LEFT_ALIGNMENT);

            // Tagline or Synopsis snippet
            String taglineText = (movie.tagline() != null && !movie.tagline().isBlank())
                    ? movie.tagline()
                    : "Directed by " + movie.director();
            JLabel taglineLabel = new JLabel(taglineText);
            taglineLabel.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(15f));
            taglineLabel.setForeground(ThemeManager.ACCENT_GOLD);
            taglineLabel.setAlignmentX(LEFT_ALIGNMENT);

            // Call to Action Buttons
            JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            actionRow.setOpaque(false);
            actionRow.setAlignmentX(LEFT_ALIGNMENT);

            // 1. Play Trailer Button
            playTrailerBtn = ThemeManager.createAccentButton("▶  Play Trailer");
            playTrailerBtn.setToolTipText("Watch official trailer on YouTube");
            playTrailerBtn.addActionListener(e -> playTrailer());

            // 2. Watchlist Button
            watchlistBtn = ThemeManager.createGhostButton("+ Watchlist");
            syncWatchlistButton();
            watchlistBtn.addActionListener(e -> toggleWatchlist());

            // 3. Movie Details Button
            JButton detailsBtn = ThemeManager.createGhostButton("ℹ  Details");
            detailsBtn.setToolTipText("View complete cast, reviews, and film rooms");
            detailsBtn.addActionListener(e -> {
                if (onMovieSelected != null) {
                    onMovieSelected.accept(movie);
                }
            });

            actionRow.add(playTrailerBtn);
            actionRow.add(watchlistBtn);
            actionRow.add(detailsBtn);

            // Assemble info block
            infoBlock.add(tagRow);
            infoBlock.add(Box.createVerticalStrut(10));
            infoBlock.add(titleLabel);
            infoBlock.add(Box.createVerticalStrut(6));
            infoBlock.add(taglineLabel);
            infoBlock.add(Box.createVerticalStrut(20));
            infoBlock.add(actionRow);

            add(infoBlock, BorderLayout.SOUTH);

            // Asynchronously load the high-res backdrop
            AsyncImageLoader.loadImage(
                    movie.getBackdropUrl("w1280"),
                    1200, 500,
                    movie.title(),
                    icon -> {
                        if (icon instanceof ImageIcon) {
                            this.backdropImage = ((ImageIcon) icon).getImage();
                            repaint();
                        }
                    }
            );
        }

        private void playTrailer() {
            if (movie == null) return;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            playTrailerBtn.setEnabled(false);

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return repository.getMovieTrailerUrl(movie);
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    playTrailerBtn.setEnabled(true);
                    String url;
                    try {
                        url = get();
                    } catch (Exception ex) {
                        url = movie.getTrailerUrl();
                    }

                    if (url == null || url.isBlank()) {
                        url = movie.getTrailerUrl();
                    }

                    TrailerPlayer.openTrailer(HeroBannerPanel.this, url, movie.title());
                }
            }.execute();
        }

        private void toggleWatchlist() {
            if (movie == null || repository == null) return;

            boolean currentlyInWatchlist = repository.isWatchlisted(movie.id());

            if (currentlyInWatchlist) {
                repository.removeFromWatchlist(movie.id());
            } else {
                repository.addToWatchlist(movie.id());
            }

            syncWatchlistButton();

            if (onWatchlistChanged != null) {
                onWatchlistChanged.run();
            }
        }

        public void syncWatchlistButton() {
            if (watchlistBtn == null || movie == null || repository == null) return;

            boolean inWatchlist = repository.isWatchlisted(movie.id());
            if (inWatchlist) {
                watchlistBtn.setText("✓  In Watchlist");
                watchlistBtn.setForeground(ThemeManager.ACCENT_GOLD);
                watchlistBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeManager.ACCENT_GOLD),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            } else {
                watchlistBtn.setText("+  Watchlist");
                watchlistBtn.setForeground(ThemeManager.TEXT_MUTED);
                watchlistBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }
            watchlistBtn.repaint();
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
                g2.drawImage(backdropImage, 0, 0, w, h, this);
            } else {
                g2.setColor(ThemeManager.BG_CARD);
                g2.fillRect(0, 0, w, h);
            }

            // 3. Draw gradient overlay to ensure text readability
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(8, 10, 14, 38),
                    0, h * 0.48f, new Color(8, 10, 14, 105)
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, w, h);
            gradient = new GradientPaint(
                    0, h * 0.30f, new Color(10, 12, 16, 20),
                    0, h, new Color(10, 12, 16, 250)
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, w, h);

            // 4. Reset clip and draw subtle border
            g2.setClip(null);
            g2.setColor(new Color(ThemeManager.ACCENT_GOLD.getRed(), ThemeManager.ACCENT_GOLD.getGreen(), ThemeManager.ACCENT_GOLD.getBlue(), 72));
            g2.drawRoundRect(0, 0, w - 1, h - 1, ThemeManager.CARD_ARC, ThemeManager.CARD_ARC);

            g2.dispose();
        }
    }
}
