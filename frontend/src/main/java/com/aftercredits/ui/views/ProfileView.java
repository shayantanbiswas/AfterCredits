package com.aftercredits.ui.views;

import com.aftercredits.client.auth.SessionManager;
import com.aftercredits.model.UserProfile;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.LoginFrame;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.DnaBarChart;
import com.aftercredits.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.time.LocalDate;

/**
 * Modern profile view displaying user identity, stats, avatar, and Film DNA.
 * Supports in-app authentication, displaying real session credentials or Guest ID.
 */
public class ProfileView extends JPanel {

    private final CinemaRepository repository;
    private final Runnable onSessionUpdated;

    public ProfileView(CinemaRepository repository) {
        this(repository, null);
    }

    public ProfileView(CinemaRepository repository, Runnable onSessionUpdated) {
        this.repository = repository;
        this.onSessionUpdated = onSessionUpdated;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        buildView();
    }

    public void refresh() {
        buildView();
    }

    private void buildView() {
        removeAll();

        UserProfile profile = repository.getUserProfile();
        var watchedMovies = repository.getWatchedMovies();
        SessionManager.Session session = SessionManager.getSession();
int personalReviewCount = repository.getPersonalReviews().size();
int filmsThisYear = (int) watchedMovies.stream()
    .filter(movie -> {
        String date = repository.getWatchedDate(movie.id());
        return date != null && date.startsWith(
            String.valueOf(LocalDate.now().getYear()));
    })
    .count();
int personalListCount = repository.getCustomWatchlists().size();
int totalWatchMinutes = watchedMovies.stream()
    .mapToInt(movie -> Math.max(0, movie.durationMinutes()))
    .sum();

String totalWatchTime = totalWatchMinutes == 0
    ? "Unavailable"
    : (totalWatchMinutes / 60) + "h " + (totalWatchMinutes % 60) + "m";

Map<Integer, Integer> decadeCounts = new java.util.TreeMap<>();
for (var movie : watchedMovies) {
    if (movie.year() > 0) {
        int decade = (movie.year() / 10) * 10;
        decadeCounts.merge(decade, 1, Integer::sum);
    }
}

String favouriteDecade = decadeCounts.isEmpty()
    ? "Not enough data"
    : decadeCounts.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(entry -> entry.getKey() + "s")
        .orElse("Not enough data");

Map<Double, Integer> ratingCounts = new java.util.TreeMap<>();
repository.getPersonalRatings().values().stream()
    .filter(rating -> rating != null && rating > 0)
    .forEach(rating -> ratingCounts.merge(rating, 1, Integer::sum));



        Map<String, Integer> genreCounts = new HashMap<>();
        int totalGenreEntries = 0;

        for (var movie : watchedMovies) {
            for (String genre : movie.genres()) {
                if (genre != null && !genre.isBlank()) {
                    genreCounts.merge(genre, 1, Integer::sum);
                    totalGenreEntries++;
                }
            }
        }

        Map<String, Integer> genrePercentages = new LinkedHashMap<>();
        final int genreTotal = totalGenreEntries;
        if (genreTotal > 0) {
            genreCounts.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .forEach(entry -> genrePercentages.put(
                            entry.getKey(),
                            (int) Math.round(entry.getValue() * 100.0 / genreTotal)
                    ));
        }

        int diversityScore = Math.min(100, genreCounts.size() * 15);
        int viewingScore = Math.min(100, watchedMovies.size() * 5);
        int dnaScore = (int) Math.round(diversityScore * 0.65 + viewingScore * 0.35);

        String dnaIdentity = "Cinema Explorer";
        if (!genreCounts.isEmpty()) {
            var topGenre = genreCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElseThrow().getKey();

            dnaIdentity = genreCounts.size() >= 6
                    ? "Genre Explorer"
                    : topGenre + " Enthusiast";
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(ThemeManager.createViewPadding());

        // Header profile card
        RoundedPanel headerCard = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        headerCard.setLayout(new BorderLayout(20, 20));
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        headerCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel avatar = new JLabel("👤", JLabel.CENTER);
        avatar.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(java.awt.Font.BOLD, 50f));
        avatar.setPreferredSize(new Dimension(104, 104));
        avatar.setOpaque(true);
        avatar.setBackground(ThemeManager.BG_INPUT);
        avatar.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        String usernameDisplay;
        String identityDisplay;
        String bioDisplay;

        if (session != null && session.isGuest()) {
            usernameDisplay = "Guest";
            identityDisplay = "Guest Cinema Explorer • ID: " + (session.getGuestId() != null ? session.getGuestId() : "Active");
            bioDisplay = "You are currently signed in with an auto-generated Guest ID ("
                    + (session.getGuestId() != null ? session.getGuestId() : "Active")
                    + "). Sign in or create an account to permanently sync your diary and Film DNA.";
        } else if (session != null) {
            usernameDisplay = session.getDisplayName() + (session.getEmail() != null ? " (" + session.getEmail() + ")" : "");
            identityDisplay = profile.cinemaIdentity() + " • " + (session.getProvider() != null ? session.getProvider() : "Local") + " Account";
            bioDisplay = profile.bio();
        } else {
            usernameDisplay = profile.username() + " (" + profile.handle() + ")";
            identityDisplay = profile.cinemaIdentity();
            bioDisplay = profile.bio();
        }

        JLabel nameLabel = new JLabel(usernameDisplay);
        nameLabel.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(java.awt.Font.BOLD, 27f));
        nameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel identityLabel = new JLabel(identityDisplay);
        identityLabel.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(java.awt.Font.BOLD, 14f));
        identityLabel.setForeground(ThemeManager.ACCENT_GOLD);

        JLabel bioLabel = new JLabel("<html><div style='width:520px'>" + bioDisplay + "</div></html>");
        bioLabel.setFont(ThemeManager.FONT_BODY.deriveFont(13f));
        bioLabel.setForeground(ThemeManager.TEXT_MUTED);

        userInfo.add(nameLabel);
        userInfo.add(Box.createVerticalStrut(4));
        userInfo.add(identityLabel);
        userInfo.add(Box.createVerticalStrut(8));
        userInfo.add(bioLabel);

        // Action button on the right
        JPanel actionCol = new JPanel();
        actionCol.setLayout(new BoxLayout(actionCol, BoxLayout.Y_AXIS));
        actionCol.setOpaque(false);

        boolean isGuest = (session == null || session.isGuest());
        JButton authBtn = isGuest
                ? ThemeManager.createAccentButton("🔐  Sign In / Sign Up")
                : ThemeManager.createGhostButton("🔄  Switch Account");

        authBtn.addActionListener(e -> {
            LoginFrame.showAsDialog(this, s -> {
                refresh();
                if (onSessionUpdated != null) {
                    onSessionUpdated.run();
                }
            });
        });

        actionCol.add(Box.createVerticalGlue());
        actionCol.add(authBtn);
        actionCol.add(Box.createVerticalGlue());

        headerCard.add(avatar, BorderLayout.WEST);
        headerCard.add(userInfo, BorderLayout.CENTER);
        headerCard.add(actionCol, BorderLayout.EAST);
        content.add(headerCard);
        content.add(Box.createVerticalStrut(26));

        // Film DNA and Stats
        JPanel lowerGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        lowerGrid.setOpaque(false);
        lowerGrid.setAlignmentX(LEFT_ALIGNMENT);

        RoundedPanel statsPanel = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        statsPanel.add(ThemeManager.createSectionHeader("Cinema Stats", null));
        statsPanel.add(Box.createVerticalStrut(16));
        statsPanel.add(createStatRow("Total Films Logged", String.valueOf(watchedMovies.size())));
        statsPanel.add(createStatRow("Films This Year", String.valueOf(filmsThisYear)));
        statsPanel.add(createStatRow("Reviews Written", String.valueOf(personalReviewCount)));
        statsPanel.add(createStatRow("Lists Created", String.valueOf(personalListCount)));

        // DNA
        RoundedPanel dnaPanel = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        dnaPanel.setLayout(new BorderLayout());
        dnaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        JPanel dnaHeader = new JPanel();
        dnaHeader.setLayout(new BoxLayout(dnaHeader, BoxLayout.Y_AXIS));
        dnaHeader.setOpaque(false);

        dnaHeader.add(ThemeManager.createSectionHeader("Film DNA", "Your viewing identity"));
        dnaHeader.add(Box.createVerticalStrut(12));

        JLabel scoreLabel = new JLabel(
                watchedMovies.size() >= 5 ? dnaScore + " / 100" : "Building your DNA"
        );
        scoreLabel.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(java.awt.Font.BOLD, 36f));
        scoreLabel.setForeground(ThemeManager.ACCENT_GOLD);
        scoreLabel.setAlignmentX(LEFT_ALIGNMENT);
        dnaHeader.add(scoreLabel);

        JLabel dnaIdentityLabel = new JLabel(
                watchedMovies.size() >= 5 ? dnaIdentity : watchedMovies.size() + " / 5 films logged"
        );
        dnaIdentityLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        dnaIdentityLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        dnaIdentityLabel.setAlignmentX(LEFT_ALIGNMENT);
        dnaHeader.add(dnaIdentityLabel);

        JLabel explanationLabel = new JLabel(
                watchedMovies.size() >= 5
                        ? "Based on your watched-film count and genre diversity."
                        : "Log at least 5 films to reveal your Film DNA."
        );
        explanationLabel.setFont(ThemeManager.FONT_SECONDARY);
        explanationLabel.setForeground(ThemeManager.TEXT_MUTED);
        explanationLabel.setAlignmentX(LEFT_ALIGNMENT);
        dnaHeader.add(Box.createVerticalStrut(6));
        dnaHeader.add(explanationLabel);

        dnaPanel.add(dnaHeader, BorderLayout.NORTH);

        if (watchedMovies.size() >= 5 && !genrePercentages.isEmpty()) {
            dnaPanel.add(new DnaBarChart(genrePercentages), BorderLayout.CENTER);
        } else {
            JLabel emptyDna = new JLabel(
                    watchedMovies.isEmpty()
                            ? "Your watched films will shape this chart."
                            : "Explore more genres to build your cinema profile.",
                    JLabel.CENTER
            );
            emptyDna.setForeground(ThemeManager.TEXT_MUTED);
            dnaPanel.add(emptyDna, BorderLayout.CENTER);
        }

        lowerGrid.add(statsPanel);
        lowerGrid.add(dnaPanel);

        content.add(lowerGrid);

        // Personal analytics — derived only from this account's library.
        RoundedPanel analyticsPanel = new RoundedPanel(
                ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        analyticsPanel.setLayout(new BoxLayout(analyticsPanel, BoxLayout.Y_AXIS));
        analyticsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        analyticsPanel.setAlignmentX(LEFT_ALIGNMENT);

        analyticsPanel.add(ThemeManager.createSectionHeader(
                "Cinema Analytics", "Your personal viewing patterns"));
        analyticsPanel.add(Box.createVerticalStrut(12));
        analyticsPanel.add(createStatRow("Total Watch Time", totalWatchTime));
        analyticsPanel.add(createStatRow("Favourite Decade", favouriteDecade));
        analyticsPanel.add(Box.createVerticalStrut(8));
        analyticsPanel.add(ThemeManager.createSectionHeader(
                "Personal Rating Distribution", null));

        if (ratingCounts.isEmpty()) {
            JLabel noRatings = new JLabel(
                    "Your personal ratings will appear here as you log films.");
            noRatings.setForeground(ThemeManager.TEXT_MUTED);
            noRatings.setFont(ThemeManager.FONT_SECONDARY);
            analyticsPanel.add(Box.createVerticalStrut(10));
            analyticsPanel.add(noRatings);
        } else {
            analyticsPanel.add(Box.createVerticalStrut(8));
            for (var entry : ratingCounts.entrySet()) {
                analyticsPanel.add(createStatRow(
                        String.format("%.1f ★", entry.getKey()),
                        entry.getValue() + (entry.getValue() == 1 ? " film" : " films")));
            }
        }

        content.add(Box.createVerticalStrut(20));
        content.add(analyticsPanel);


        add(ThemeManager.createStyledScrollPane(content), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createStatRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(java.awt.Font.BOLD, 14f));
        val.setForeground(ThemeManager.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }
}
