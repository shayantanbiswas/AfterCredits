package com.aftercredits.ui.views;

import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.model.Movie;
import com.aftercredits.model.Review;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.util.AsyncImageLoader;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.time.LocalDate;
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

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.BG_PRIMARY);
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

        JButton reviewButton =
                ThemeManager.createAccentButton("★  Rate & Review");
        reviewButton.addActionListener(e -> showReviewDialog());

        JButton watchlistButton =
                ThemeManager.createGhostButton("+  Watchlist");
        watchlistButton.addActionListener(e -> {
            repository.addToWatchlist(movie.id());
            JOptionPane.showMessageDialog(
                    this,
                    movie.title() + " added to your watchlist.",
                    "Watchlist",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        JButton watchedButton =
                ThemeManager.createGhostButton("✓  Log Watched");
        watchedButton.addActionListener(e -> showWatchedDialog());

        actions.add(reviewButton);
        actions.add(watchlistButton);
        actions.add(watchedButton);

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
        // LOWER CONTENT — SOCIAL CINEMA
        // ---------------------------------------------------------
        JPanel lower = new JPanel();
        lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
        lower.setOpaque(false);
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
        scroll.getVerticalScrollBar().setUnitIncrement(18);
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
}
