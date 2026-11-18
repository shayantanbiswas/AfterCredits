package com.aftercredits.ui.views;

import com.aftercredits.model.CustomWatchlist;
import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.model.Movie;
import com.aftercredits.model.Review;
import com.aftercredits.model.VideoClip;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.RoundedPanel;
import com.aftercredits.ui.util.AsyncImageLoader;
import com.aftercredits.ui.util.TrailerPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MovieDetailView extends JPanel {

    private final Movie movie;
    private final CinemaRepository repository;

    public MovieDetailView(
            Movie movie,
            CinemaRepository repository,
            Runnable onBack) {

        this.movie = movie;
        this.repository = repository;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setOpaque(true);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.BG_PRIMARY);
        content.setOpaque(true);
        content.setBorder(BorderFactory.createEmptyBorder(14, 24, 24, 24));

        // ---------------------------------------------------------
        // TOP NAVIGATION
        // ---------------------------------------------------------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setAlignmentX(LEFT_ALIGNMENT);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton backButton = ThemeManager.createGhostButton("←  Back");
        backButton.addActionListener(e -> onBack.run());

        JLabel pageLabel = new JLabel("MOVIE");
        pageLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 11f));
        pageLabel.setForeground(ThemeManager.TEXT_FAINT);

        topBar.add(backButton, BorderLayout.WEST);
        topBar.add(pageLabel, BorderLayout.EAST);

        content.add(topBar);
        content.add(Box.createVerticalStrut(10));

        // ---------------------------------------------------------
        // CINEMATIC HERO
        // ---------------------------------------------------------
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(ThemeManager.BG_CARD);
        hero.setPreferredSize(new Dimension(0, 300));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        hero.setAlignmentX(LEFT_ALIGNMENT);
        hero.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR));

        JLabel backdrop = new JLabel();
        backdrop.setHorizontalAlignment(SwingConstants.CENTER);
        backdrop.setVerticalAlignment(SwingConstants.CENTER);
        backdrop.setOpaque(true);
        backdrop.setBackground(ThemeManager.BG_CARD);

        if (movie.backdropPath() != null && !movie.backdropPath().isBlank()) {
            AsyncImageLoader.loadImage(
                    movie.getBackdropUrl("w1280"),
                    1200,
                    300,
                    movie.title(),
                    icon -> {
                        backdrop.setIcon(icon);
                        backdrop.repaint();
                    }
            );
        }

        JPanel heroOverlay = new JPanel(new BorderLayout());
        heroOverlay.setOpaque(false);
        heroOverlay.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        JLabel heroHint = new JLabel("AFTERCREDITS  •  MOVIE DETAILS");
        heroHint.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        heroHint.setForeground(Color.WHITE);

        heroOverlay.add(heroHint, BorderLayout.NORTH);

        hero.add(backdrop, BorderLayout.CENTER);
        hero.add(heroOverlay, BorderLayout.SOUTH);

        content.add(hero);
        content.add(Box.createVerticalStrut(16));

        // ---------------------------------------------------------
        // MOVIE IDENTITY CARD
        // ---------------------------------------------------------
        JPanel identity = new JPanel(new BorderLayout(24, 0)) {
            @Override
            public Dimension getMaximumSize() {
                // Full width, but never taller than its content
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        identity.setBackground(ThemeManager.BG_CARD);
        identity.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        identity.setAlignmentX(LEFT_ALIGNMENT);

        // Poster
        JPanel posterFrame = new JPanel(new BorderLayout());
        posterFrame.setBackground(ThemeManager.BG_PRIMARY);
        posterFrame.setPreferredSize(new Dimension(222, 332));
        posterFrame.setMinimumSize(new Dimension(222, 332));
        posterFrame.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR));

        JLabel poster = new JLabel();
        poster.setHorizontalAlignment(SwingConstants.CENTER);
        poster.setVerticalAlignment(SwingConstants.CENTER);

        AsyncImageLoader.loadImage(
                movie.getPosterUrl("w500"),
                220,
                330,
                movie.title(),
                poster::setIcon
        );

        posterFrame.add(poster, BorderLayout.CENTER);

        JPanel posterColumn = new JPanel(new BorderLayout());
        posterColumn.setOpaque(false);
        posterColumn.add(posterFrame, BorderLayout.NORTH);
        identity.add(posterColumn, BorderLayout.WEST);

        // Information
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel eyebrow = new JLabel("NOW DISCOVERING");
        eyebrow.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        eyebrow.setForeground(ThemeManager.ACCENT_GOLD);
        eyebrow.setAlignmentX(LEFT_ALIGNMENT);
        info.add(eyebrow);
        info.add(Box.createVerticalStrut(5));

        JLabel title = new JLabel(movie.title());
        title.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(Font.BOLD, 34f));
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        info.add(title);

        if (movie.originalTitle() != null
                && !movie.originalTitle().isBlank()
                && !movie.originalTitle().equalsIgnoreCase(movie.title())) {

            JLabel original = new JLabel(movie.originalTitle());
            original.setFont(ThemeManager.FONT_BODY.deriveFont(Font.ITALIC));
            original.setForeground(ThemeManager.TEXT_MUTED);
            original.setAlignmentX(LEFT_ALIGNMENT);

            info.add(Box.createVerticalStrut(3));
            info.add(original);
        }

        info.add(Box.createVerticalStrut(10));

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        meta.setOpaque(false);
        meta.setAlignmentX(LEFT_ALIGNMENT);

        if (movie.year() > 0) {
            meta.add(BadgeLabel.createYearBadge(movie.year()));
        }

        if (movie.tmdbVoteAverage() > 0) {
            meta.add(new BadgeLabel(
                    String.format("TMDB ★ %.1f", movie.tmdbVoteAverage())
            ));
        }

        if (movie.tmdbVoteCount() > 0) {
            meta.add(new BadgeLabel(
                    String.format("%,d votes", movie.tmdbVoteCount())
            ));
        }

        if (movie.durationMinutes() > 0) {
            meta.add(new BadgeLabel(movie.getFormattedDuration()));
        }

        if (!movie.getGenresFormatted().equals("Unknown")) {
            meta.add(new BadgeLabel(movie.getGenresFormatted()));
        }

        info.add(meta);
        info.add(Box.createVerticalStrut(12));

        if (movie.tagline() != null && !movie.tagline().isBlank()) {
            JLabel tagline = new JLabel(
                    "<html><i>\"" + escape(movie.tagline()) + "\"</i></html>"
            );
            tagline.setFont(ThemeManager.FONT_SECTION_HEADING);
            tagline.setForeground(ThemeManager.ACCENT_GOLD);
            tagline.setAlignmentX(LEFT_ALIGNMENT);

            info.add(tagline);
            info.add(Box.createVerticalStrut(10));
        }

        JLabel director = new JLabel(
                "<html><b>Director</b>&nbsp;&nbsp;" +
                        escape(movie.director()) +
                        "</html>"
        );
        director.setFont(ThemeManager.FONT_BODY);
        director.setForeground(ThemeManager.TEXT_PRIMARY);
        director.setAlignmentX(LEFT_ALIGNMENT);

        info.add(director);
        info.add(Box.createVerticalStrut(10));

        JLabel synopsis = new JLabel(
                "<html><body style='width:600px'>" +
                        "<b>Synopsis</b><br>" +
                        escape(movie.synopsis()) +
                        "</body></html>"
        );
        synopsis.setFont(ThemeManager.FONT_BODY);
        synopsis.setForeground(ThemeManager.TEXT_MUTED);
        synopsis.setAlignmentX(LEFT_ALIGNMENT);

        info.add(synopsis);
        info.add(Box.createVerticalStrut(10));

        JLabel cast = new JLabel(
                "<html><body style='width:600px'>" +
                        "<b>Cast</b>&nbsp;&nbsp;" +
                        escape(movie.getCastFormatted()) +
                        "</body></html>"
        );
        cast.setFont(ThemeManager.FONT_BODY);
        cast.setForeground(ThemeManager.TEXT_MUTED);
        cast.setAlignmentX(LEFT_ALIGNMENT);

        info.add(cast);
        info.add(Box.createVerticalStrut(14));

        // Social actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton trailerButton =
                ThemeManager.createAccentButton("▶  Play Trailer");
        trailerButton.addActionListener(e -> {
            String url = repository.getMovieTrailerUrl(movie);
            com.aftercredits.ui.util.TrailerPlayer.openTrailer(this, url, movie.title());
        });

        JButton reviewButton =
                ThemeManager.createGhostButton("★  Rate & Review");
        reviewButton.addActionListener(e -> showReviewDialog());

        JButton watchlistButton = ThemeManager.createGhostButton("+  Watchlist");
        updateWatchlistButton(watchlistButton);
        watchlistButton.addActionListener(e -> showWatchlistSelectionDialog(watchlistButton));

        JButton watchedButton =
                ThemeManager.createGhostButton("✓  Log Watched");
        watchedButton.addActionListener(e -> showWatchedDialog());

        JButton favouriteButton = ThemeManager.createGhostButton("♥  Favourite");
        updateFavouriteButton(favouriteButton);
        favouriteButton.addActionListener(e -> {
            repository.toggleFavourite(movie.id());
            updateFavouriteButton(favouriteButton);
        });

        actions.add(trailerButton);
        actions.add(reviewButton);
        actions.add(watchlistButton);
        actions.add(watchedButton);
        actions.add(favouriteButton);

        if (movie.getImdbUrl() != null && !movie.getImdbUrl().isBlank()) {
            JButton imdbButton = ThemeManager.createGhostButton("IMDb ↗");
            imdbButton.addActionListener(e -> openUrl(movie.getImdbUrl()));
            actions.add(imdbButton);
        }

        info.add(actions);
        identity.add(info, BorderLayout.CENTER);

        content.add(identity);
        content.add(Box.createVerticalStrut(22));

        // ---------------------------------------------------------
        // MOVIE PREVIEWS & TRAILERS
        // ---------------------------------------------------------
        content.add(createPreviewsSection());
        content.add(Box.createVerticalStrut(22));

        // ---------------------------------------------------------
        // LOWER CONTENT — SOCIAL CINEMA
        // ---------------------------------------------------------
        JPanel lower = new JPanel();
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        lower.setBackground(ThemeManager.BG_PRIMARY);
        lower.setOpaque(true);
        lower.setAlignmentX(LEFT_ALIGNMENT);
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel socialEyebrow = new JLabel("THE COMMUNITY");
        socialEyebrow.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        socialEyebrow.setForeground(ThemeManager.ACCENT_GOLD);
        socialEyebrow.setAlignmentX(LEFT_ALIGNMENT);

        lower.add(socialEyebrow);
        lower.add(Box.createVerticalStrut(4));

        lower.add(createSectionTitle("Film Room"));
        lower.add(Box.createVerticalStrut(8));
        lower.add(createFilmRoomSection());

        lower.add(Box.createVerticalStrut(22));

        lower.add(createSectionTitle("Community Reviews"));
        lower.add(Box.createVerticalStrut(8));
        lower.add(createReviewsSection());

        content.add(lower);

        JScrollPane scroll = ThemeManager.createStyledScrollPane(content);
        // 32px unit increment already set in ThemeManager; reinforce for this detail view
        scroll.getVerticalScrollBar().setUnitIncrement(32);
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(Font.BOLD, 24f));
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createFilmRoomSection() {
        JPanel panel = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_CARD);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JButton startTopic =
                ThemeManager.createAccentButton("+ Start a Film Room discussion");

        startTopic.setAlignmentX(LEFT_ALIGNMENT);
        startTopic.addActionListener(e -> showFilmRoomDialog());

        panel.add(startTopic);
        panel.add(Box.createVerticalStrut(10));

        var topics = repository.getTopicsForMovie(movie.id());

        if (topics.isEmpty()) {
            JLabel empty = new JLabel(
                    "No discussions yet. Be the first person to start one."
            );
            empty.setForeground(ThemeManager.TEXT_MUTED);
            empty.setFont(ThemeManager.FONT_BODY);
            panel.add(empty);
            return panel;
        }

        for (FilmRoomTopic topic : topics) {
            JPanel topicPanel = new JPanel();
            topicPanel.setLayout(new BoxLayout(topicPanel, BoxLayout.Y_AXIS));
            topicPanel.setOpaque(false);
            topicPanel.setBorder(
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    new Color(255, 255, 255, 25)
                            ),
                            BorderFactory.createEmptyBorder(12, 14, 12, 14)
                    )
            );

            JLabel topicTitle = new JLabel(
                    topic.topicTitle() +
                            (topic.hasSpoilers() ? "  ⚠ Spoilers" : "")
            );
            topicTitle.setFont(ThemeManager.FONT_BODY.deriveFont(Font.BOLD));
            topicTitle.setForeground(ThemeManager.TEXT_PRIMARY);

            JLabel topicMeta = new JLabel(
                    topic.category() +
                            " · " +
                            topic.author() +
                            " · " +
                            topic.commentCount() +
                            " comments · ↑ " +
                            topic.upvotes()
            );
            topicMeta.setFont(ThemeManager.FONT_BODY);
            topicMeta.setForeground(ThemeManager.TEXT_MUTED);

            JLabel topicContent = new JLabel(
                    "<html><body style='width:700px'>" +
                            escape(topic.content()) +
                            "</body></html>"
            );
            topicContent.setFont(ThemeManager.FONT_BODY);
            topicContent.setForeground(ThemeManager.TEXT_MUTED);

            topicPanel.add(topicTitle);
            topicPanel.add(Box.createVerticalStrut(5));
            topicPanel.add(topicMeta);
            topicPanel.add(Box.createVerticalStrut(8));
            topicPanel.add(topicContent);

            panel.add(topicPanel);
            panel.add(Box.createVerticalStrut(10));
        }

        return panel;
    }

    private JPanel createReviewsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        var reviews = repository.getCommunityReviews()
                .stream()
                .filter(r -> movie.id().equals(r.movieId()))
                .toList();

        if (reviews.isEmpty()) {
            JLabel empty = new JLabel(
                    "No community reviews yet. Write the first one."
            );
            empty.setFont(ThemeManager.FONT_BODY);
            empty.setForeground(ThemeManager.TEXT_MUTED);
            panel.add(empty);
            return panel;
        }

        for (Review review : reviews) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(ThemeManager.BG_CARD);
            card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
            card.setAlignmentX(LEFT_ALIGNMENT);

            JLabel header = new JLabel(
                    review.author() +
                            "   " +
                            review.getFormattedRating() +
                            "   ·   " +
                            review.dateLogged()
            );
            header.setFont(ThemeManager.FONT_BODY.deriveFont(Font.BOLD));
            header.setForeground(ThemeManager.TEXT_PRIMARY);

            JLabel body = new JLabel(
                    "<html><body style='width:760px'>" +
                            escape(review.comment()) +
                            "</body></html>"
            );
            body.setFont(ThemeManager.FONT_BODY);
            body.setForeground(ThemeManager.TEXT_MUTED);

            card.add(header);
            card.add(Box.createVerticalStrut(10));
            card.add(body);

            if (review.containsSpoilers()) {
                JLabel spoiler = new JLabel("⚠ Contains spoilers");
                spoiler.setForeground(ThemeManager.ACCENT_GOLD);
                spoiler.setFont(ThemeManager.FONT_BODY);
                card.add(Box.createVerticalStrut(8));
                card.add(spoiler);
            }

            panel.add(card);
            panel.add(Box.createVerticalStrut(10));
        }

        return panel;
    }

    private void showReviewDialog() {
        JTextArea comment = new JTextArea(7, 45);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);

        JSpinner rating = new JSpinner(
                new SpinnerNumberModel(4.0, 0.5, 5.0, 0.5)
        );

        JCheckBox spoilers =
                new JCheckBox("Contains spoilers");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Your rating (0.5 – 5.0):"));
        panel.add(rating);
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Your review:"));
        panel.add(new JScrollPane(comment));
        panel.add(Box.createVerticalStrut(8));
        panel.add(spoilers);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Review " + movie.title(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String text = comment.getText().trim();

        if (text.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Write something before submitting your review."
            );
            return;
        }

        double value = ((Number) rating.getValue()).doubleValue();

        Review review = new Review(
                UUID.randomUUID().toString(),
                movie.id(),
                movie.title(),
                "You",
                value,
                text,
                LocalDate.now().toString(),
                spoilers.isSelected()
        );

        repository.addReview(review);

        JOptionPane.showMessageDialog(
                this,
                "Your review has been added.",
                "Review saved",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showWatchedDialog() {
        JSpinner rating = new JSpinner(
                new SpinnerNumberModel(4.0, 0.5, 5.0, 0.5)
        );

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Your rating:"));
        panel.add(rating);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Log " + movie.title(),
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        double value = ((Number) rating.getValue()).doubleValue();

        repository.logMovieAsWatched(movie.id(), value);

        JOptionPane.showMessageDialog(
                this,
                movie.title() + " was added to your diary.",
                "Diary",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showFilmRoomDialog() {
        JTextField topicTitle = new JTextField();
        JTextField category = new JTextField("Discussion");

        JTextArea content = new JTextArea(7, 45);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);

        JCheckBox spoilers =
                new JCheckBox("Contains spoilers");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Topic title:"));
        panel.add(topicTitle);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Category:"));
        panel.add(category);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Discussion:"));
        panel.add(new JScrollPane(content));
        panel.add(Box.createVerticalStrut(8));

        panel.add(spoilers);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Film Room · " + movie.title(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String title = topicTitle.getText().trim();
        String body = content.getText().trim();
        String categoryText = category.getText().trim();

        if (title.isBlank() || body.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both a topic title and discussion."
            );
            return;
        }

        FilmRoomTopic topic = new FilmRoomTopic(
                UUID.randomUUID().toString(),
                movie.id(),
                movie.title(),
                title,
                categoryText.isBlank() ? "Discussion" : categoryText,
                "You",
                body,
                spoilers.isSelected(),
                0,
                0
        );

        repository.addFilmRoomTopic(topic);

        JOptionPane.showMessageDialog(
                this,
                "Your Film Room discussion has been created.",
                "Film Room",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not open the link."
            );
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void updateFavouriteButton(JButton button) {
        boolean favourite = repository.isFavourite(movie.id());
        button.setText(favourite ? "♥  Favourited" : "♥  Favourite");
    }

    private void updateWatchlistButton(JButton button) {
        boolean inDefault = repository.isWatchlisted(movie.id());
        boolean inCustom = !repository.getWatchlistsContainingMovie(movie.id()).isEmpty();
        if (inDefault || inCustom) {
            button.setText("✓  In Watchlist");
            button.setForeground(ThemeManager.ACCENT_GOLD);
        } else {
            button.setText("+  Watchlist");
            button.setForeground(ThemeManager.TEXT_MUTED);
        }
    }

    private void showWatchlistSelectionDialog(JButton watchlistButton) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(
                parentWindow,
                "Watchlists — " + movie.title(),
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(440, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("Save to Watchlists");
        titleLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Add or remove \"" + movie.title() + "\" from your lists:");
        subtitleLabel.setFont(ThemeManager.FONT_SECONDARY);
        subtitleLabel.setForeground(ThemeManager.TEXT_MUTED);

        JLabel statusFeedback = new JLabel(" ");
        statusFeedback.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 11f));
        statusFeedback.setForeground(ThemeManager.ACCENT_GOLD);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(statusFeedback);
        header.add(Box.createVerticalStrut(10));
        header.add(ThemeManager.createDivider());

        panel.add(header, BorderLayout.NORTH);

        // List container
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        Runnable refreshList = new Runnable() {
            @Override
            public void run() {
                listContainer.removeAll();

                // 1. Default Watchlist
                boolean inDefault = repository.isWatchlisted(movie.id());
                JCheckBox defaultCheck = new JCheckBox("★  Default Watchlist", inDefault);
                defaultCheck.setFont(ThemeManager.FONT_BUTTON);
                defaultCheck.setForeground(ThemeManager.TEXT_PRIMARY);
                defaultCheck.setOpaque(false);
                defaultCheck.addActionListener(e -> {
                    if (defaultCheck.isSelected()) {
                        repository.addToWatchlist(movie.id());
                        statusFeedback.setText("✓ Added to Default Watchlist");
                    } else {
                        repository.removeFromWatchlist(movie.id());
                        statusFeedback.setText("Removed from Default Watchlist");
                    }
                    updateWatchlistButton(watchlistButton);
                });

                listContainer.add(defaultCheck);
                listContainer.add(Box.createVerticalStrut(12));
                listContainer.add(ThemeManager.createDivider());
                listContainer.add(Box.createVerticalStrut(12));

                // Section label for custom watchlists
                JLabel customHeader = new JLabel("CUSTOM WATCHLISTS");
                customHeader.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
                customHeader.setForeground(ThemeManager.ACCENT_GOLD);
                listContainer.add(customHeader);
                listContainer.add(Box.createVerticalStrut(8));

                List<CustomWatchlist> customLists = repository.getCustomWatchlists();
                if (customLists.isEmpty()) {
                    JLabel emptyLabel = new JLabel("<html><i>No custom watchlists created yet.<br>Click below to create your first list!</i></html>");
                    emptyLabel.setFont(ThemeManager.FONT_SECONDARY);
                    emptyLabel.setForeground(ThemeManager.TEXT_MUTED);
                    listContainer.add(emptyLabel);
                } else {
                    for (CustomWatchlist wl : customLists) {
                        boolean inWl = wl.containsMovie(movie.id());
                        String label = String.format("%s  (%s • %d films)", wl.getName(), wl.getPrivacyLabel(), wl.getMovieCount());
                        JCheckBox wlCheck = new JCheckBox(label, inWl);
                        wlCheck.setFont(ThemeManager.FONT_BODY);
                        wlCheck.setForeground(ThemeManager.TEXT_PRIMARY);
                        wlCheck.setOpaque(false);
                        wlCheck.addActionListener(e -> {
                            if (wlCheck.isSelected()) {
                                repository.addMovieToCustomWatchlist(wl.getId(), movie.id());
                                statusFeedback.setText("✓ Added to \"" + wl.getName() + "\"");
                            } else {
                                repository.removeMovieFromCustomWatchlist(wl.getId(), movie.id());
                                statusFeedback.setText("Removed from \"" + wl.getName() + "\"");
                            }
                            updateWatchlistButton(watchlistButton);
                        });
                        listContainer.add(wlCheck);
                        listContainer.add(Box.createVerticalStrut(8));
                    }
                }

                listContainer.revalidate();
                listContainer.repaint();
            }
        };

        refreshList.run();

        JScrollPane scroll = ThemeManager.createStyledScrollPane(listContainer);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        // Footer actions
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JButton createNewBtn = ThemeManager.createGhostButton("+  Create New Watchlist");
        createNewBtn.addActionListener(e -> {
            showCreateWatchlistDialog(dialog, () -> {
                refreshList.run();
                updateWatchlistButton(watchlistButton);
                statusFeedback.setText("✓ Watchlist created and movie added");
            });
        });

        JButton doneBtn = ThemeManager.createAccentButton("Done");
        doneBtn.addActionListener(e -> dialog.dispose());

        footer.add(createNewBtn, BorderLayout.WEST);
        footer.add(doneBtn, BorderLayout.EAST);

        panel.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showCreateWatchlistDialog(Window parent, Runnable onCreated) {
        JDialog dialog = new JDialog(parent, "Create Watchlist", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel("Create Custom Watchlist");
        titleLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(14));

        JLabel nameLabel = new JLabel("Watchlist Name *");
        nameLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        nameLabel.setForeground(ThemeManager.ACCENT_GOLD);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(4));

        JTextField nameField = ThemeManager.createStyledTextField();
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel("Description (Optional)");
        descLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        descLabel.setForeground(ThemeManager.TEXT_MUTED);
        descLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(4));

        JTextField descField = ThemeManager.createStyledTextField();
        descField.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(descField);
        panel.add(Box.createVerticalStrut(12));

        JLabel privacyLabel = new JLabel("Privacy Setting");
        privacyLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        privacyLabel.setForeground(ThemeManager.ACCENT_GOLD);
        privacyLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(privacyLabel);
        panel.add(Box.createVerticalStrut(6));

        JRadioButton privateRadio = new JRadioButton("🔒 Private (Only visible to you)", true);
        privateRadio.setFont(ThemeManager.FONT_BODY);
        privateRadio.setForeground(ThemeManager.TEXT_PRIMARY);
        privateRadio.setOpaque(false);
        privateRadio.setAlignmentX(LEFT_ALIGNMENT);

        JRadioButton publicRadio = new JRadioButton("🌐 Public (Visible on your profile & to community)", false);
        publicRadio.setFont(ThemeManager.FONT_BODY);
        publicRadio.setForeground(ThemeManager.TEXT_PRIMARY);
        publicRadio.setOpaque(false);
        publicRadio.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup privacyGroup = new ButtonGroup();
        privacyGroup.add(privateRadio);
        privacyGroup.add(publicRadio);

        panel.add(privateRadio);
        panel.add(Box.createVerticalStrut(4));
        panel.add(publicRadio);
        panel.add(Box.createVerticalStrut(18));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = ThemeManager.createGhostButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton createBtn = ThemeManager.createAccentButton("Create & Add Movie");
        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a watchlist name.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean isPrivate = privateRadio.isSelected();
            CustomWatchlist newWl = repository.createCustomWatchlist(name, descField.getText().trim(), isPrivate);
            repository.addMovieToCustomWatchlist(newWl.getId(), movie.id());
            dialog.dispose();
            if (onCreated != null) {
                onCreated.run();
            }
        });

        buttons.add(cancelBtn);
        buttons.add(createBtn);
        panel.add(buttons);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JPanel createPreviewsSection() {
        JPanel section = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        section.setLayout(new BorderLayout(0, 10));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        // Header Panel: Eyebrow + Title/Badge on Left, Navigation Carousel Arrows on Right
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(LEFT_ALIGNMENT);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JPanel leftTitles = new JPanel();
        leftTitles.setLayout(new BoxLayout(leftTitles, BoxLayout.Y_AXIS));
        leftTitles.setOpaque(false);

        JLabel eyebrow = new JLabel("OFFICIAL FOOTAGE & PREVIEWS");
        eyebrow.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        eyebrow.setForeground(ThemeManager.ACCENT_GOLD);
        eyebrow.setAlignmentX(LEFT_ALIGNMENT);
        leftTitles.add(eyebrow);
        leftTitles.add(Box.createVerticalStrut(3));

        JPanel titleWithBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleWithBadge.setOpaque(false);
        titleWithBadge.setAlignmentX(LEFT_ALIGNMENT);
        JLabel title = createSectionTitle("Trailers, Clips & Previews");
        titleWithBadge.add(title);

        List<VideoClip> previews = repository.getMoviePreviews(movie);
        titleWithBadge.add(new BadgeLabel(previews.size() + " clips"));
        leftTitles.add(titleWithBadge);

        headerPanel.add(leftTitles, BorderLayout.WEST);

        // Carousel arrow navigation buttons
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navButtons.setOpaque(false);

        JButton prevBtn = ThemeManager.createGhostButton("‹");
        prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        prevBtn.setPreferredSize(new Dimension(36, 32));
        prevBtn.setToolTipText("Scroll previous previews");

        JButton nextBtn = ThemeManager.createGhostButton("›");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nextBtn.setPreferredSize(new Dimension(36, 32));
        nextBtn.setToolTipText("Scroll next previews");

        navButtons.add(prevBtn);
        navButtons.add(nextBtn);
        headerPanel.add(navButtons, BorderLayout.EAST);

        section.add(headerPanel, BorderLayout.NORTH);

        // Cards row with stable geometry and padding
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentY(Component.TOP_ALIGNMENT);
        cardsRow.setFocusable(false);

        for (VideoClip clip : previews) {
            cardsRow.add(createPreviewCard(clip));
        }

        JScrollPane scroll = new JScrollPane(cardsRow);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);
        scroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 222));
        scroll.setMinimumSize(new Dimension(0, 222));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 222));
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setFocusable(false);

        int cardStep = 286;
        prevBtn.addActionListener(e -> {
            JScrollBar hbar = scroll.getHorizontalScrollBar();
            hbar.setValue(Math.max(0, hbar.getValue() - cardStep));
        });
        nextBtn.addActionListener(e -> {
            JScrollBar hbar = scroll.getHorizontalScrollBar();
            hbar.setValue(Math.min(hbar.getMaximum(), hbar.getValue() + cardStep));
        });

        // Smooth horizontal mouse wheel forwarding
        scroll.addMouseWheelListener(e -> {
            JScrollBar hbar = scroll.getHorizontalScrollBar();
            int move = e.getWheelRotation() * hbar.getUnitIncrement() * 3;
            hbar.setValue(hbar.getValue() + move);
            e.consume();
        });

        section.add(scroll, BorderLayout.CENTER);
        return section;
    }

    private JPanel createPreviewCard(VideoClip clip) {
        RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        card.setLayout(new BorderLayout(0, 0));
        card.setPreferredSize(new Dimension(270, 190));
        card.setMinimumSize(new Dimension(270, 190));
        card.setMaximumSize(new Dimension(270, 190));
        card.setAlignmentY(Component.TOP_ALIGNMENT);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Fixed video thumbnail area (270x130)
        JPanel thumbContainer = new JPanel(new BorderLayout());
        thumbContainer.setPreferredSize(new Dimension(270, 130));
        thumbContainer.setMinimumSize(new Dimension(270, 130));
        thumbContainer.setMaximumSize(new Dimension(270, 130));
        thumbContainer.setBackground(ThemeManager.BG_PRIMARY);
        thumbContainer.setOpaque(true);

        JLabel thumbLabel = new JLabel();
        thumbLabel.setHorizontalAlignment(SwingConstants.CENTER);
        thumbLabel.setVerticalAlignment(SwingConstants.CENTER);
        thumbLabel.setLayout(new BorderLayout());

        String thumbUrl = clip.getThumbnailUrl();
        if (thumbUrl == null || thumbUrl.isBlank()) {
            thumbUrl = movie.getBackdropUrl("w780");
        }
        if (thumbUrl == null || thumbUrl.isBlank()) {
            thumbUrl = movie.getPosterUrl("w500");
        }

        if (thumbUrl != null && !thumbUrl.isBlank()) {
            AsyncImageLoader.loadImage(thumbUrl, 270, 130, clip.name(), thumbLabel::setIcon);
        }

        // Top badges: Category badge
        JPanel topBadgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        topBadgePanel.setOpaque(false);
        topBadgePanel.add(new BadgeLabel(clip.getBadgeLabel()));
        thumbLabel.add(topBadgePanel, BorderLayout.NORTH);

        // Center glowing play button overlay
        JLabel playIcon = new JLabel("▶", SwingConstants.CENTER);
        playIcon.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(Font.BOLD, 24f));
        playIcon.setForeground(ThemeManager.ACCENT_GOLD);
        thumbLabel.add(playIcon, BorderLayout.CENTER);

        // Bottom duration / quality badge
        JPanel bottomBadgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bottomBadgePanel.setOpaque(false);
        bottomBadgePanel.add(new BadgeLabel("YouTube • HD"));
        thumbLabel.add(bottomBadgePanel, BorderLayout.SOUTH);

        thumbContainer.add(thumbLabel, BorderLayout.CENTER);
        card.add(thumbContainer, BorderLayout.NORTH);

        // Fixed bottom info area (270x60)
        JPanel bottomInfo = new JPanel();
        bottomInfo.setLayout(new BoxLayout(bottomInfo, BoxLayout.Y_AXIS));
        bottomInfo.setOpaque(false);
        bottomInfo.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        bottomInfo.setPreferredSize(new Dimension(270, 60));
        bottomInfo.setMinimumSize(new Dimension(270, 60));
        bottomInfo.setMaximumSize(new Dimension(270, 60));

        String displayName = clip.name();
        if (displayName.length() > 34) {
            displayName = displayName.substring(0, 32) + "...";
        }
        JLabel titleLabel = new JLabel(displayName);
        titleLabel.setFont(ThemeManager.FONT_BODY.deriveFont(Font.BOLD, 12.5f));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        titleLabel.setToolTipText(clip.name());

        JLabel clickHint = new JLabel("▶  Play Preview ↗");
        clickHint.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10.5f));
        clickHint.setForeground(ThemeManager.ACCENT_GOLD);
        clickHint.setAlignmentX(LEFT_ALIGNMENT);

        bottomInfo.add(titleLabel);
        bottomInfo.add(Box.createVerticalStrut(3));
        bottomInfo.add(clickHint);

        card.add(bottomInfo, BorderLayout.CENTER);

        // Smooth hover and click interaction
        MouseAdapter hoverAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TrailerPlayer.openTrailer(MovieDetailView.this, clip.getPlayUrl(movie.title()), clip.name());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorderColor(ThemeManager.ACCENT_GOLD);
                playIcon.setForeground(Color.WHITE);
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorderColor(ThemeManager.BORDER_COLOR);
                playIcon.setForeground(ThemeManager.ACCENT_GOLD);
                card.repaint();
            }
        };

        card.addMouseListener(hoverAdapter);
        thumbContainer.addMouseListener(hoverAdapter);
        thumbLabel.addMouseListener(hoverAdapter);
        bottomInfo.addMouseListener(hoverAdapter);
        titleLabel.addMouseListener(hoverAdapter);
        clickHint.addMouseListener(hoverAdapter);

        return card;
    }
}
