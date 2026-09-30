package com.aftercredits.ui.views;

import com.aftercredits.model.UserProfile;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.DnaBarChart;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * The user profile and taste analytics screen for AfterCredits.
 * Displays personal details, Cinema Identity classification, stat counters,
 * and the signature Film DNA breakdown chart.
 */
public class ProfileView extends JPanel {

    private final CinemaRepository repository;

    public ProfileView(CinemaRepository repository) {
        this.repository = repository;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ThemeManager.BG_PRIMARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        UserProfile profile = repository.getUserProfile();

        // 1. Profile Header Card (Avatar, Identity, Bio)
        contentPanel.add(createProfileHeader(profile));
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Cinema Statistics Grid (Logged, This Year, Lists, Reviews)
        contentPanel.add(createStatsGrid(profile));
        contentPanel.add(Box.createVerticalStrut(24));

        // 3. Film DNA Taste Distribution Section
        contentPanel.add(createFilmDnaSection(profile));
        contentPanel.add(Box.createVerticalStrut(20));

        // 4. Favorite Auteurs & Directors Section
        contentPanel.add(createDirectorsSection(profile));

        // Scroll pane wrapper
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createProfileHeader(UserProfile profile) {
        JPanel headerCard = new JPanel(new BorderLayout(20, 0));
        headerCard.setBackground(ThemeManager.BG_CARD);
        headerCard.setBorder(ThemeManager.createCardBorder());
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Left Avatar placeholder
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setPreferredSize(new Dimension(80, 80));
        avatarPanel.setBackground(ThemeManager.BG_PRIMARY);
        avatarPanel.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1, true));

        JLabel avatarIcon = new JLabel("🎬", JLabel.CENTER);
        avatarIcon.setFont(ThemeManager.FONT_TITLE_LARGE.deriveFont(28f));
        avatarPanel.add(avatarIcon, BorderLayout.CENTER);

        // Center Profile Info
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);

        // Identity Badge + Handle
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(BadgeLabel.createAccentBadge("🧬 CINEMA IDENTITY: " + profile.cinemaIdentity().toUpperCase()));
        topRow.add(new BadgeLabel(profile.handle()));

        JLabel usernameLabel = new JLabel(profile.username());
        usernameLabel.setFont(ThemeManager.FONT_TITLE_LARGE);
        usernameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel bioLabel = new JLabel("<html><body style='width: 500px;'>" + profile.bio() + "</body></html>");
        bioLabel.setFont(ThemeManager.FONT_BODY);
        bioLabel.setForeground(ThemeManager.TEXT_MUTED);

        detailsPanel.add(topRow);
        detailsPanel.add(Box.createVerticalStrut(6));
        detailsPanel.add(usernameLabel);
        detailsPanel.add(Box.createVerticalStrut(4));
        detailsPanel.add(bioLabel);

        headerCard.add(avatarPanel, BorderLayout.WEST);
        headerCard.add(detailsPanel, BorderLayout.CENTER);

        return headerCard;
    }

    private JPanel createStatsGrid(UserProfile profile) {
        JPanel grid = new JPanel(new GridLayout(1, 4, 14, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        grid.add(createStatCard(String.valueOf(profile.totalFilmsWatched()), "Total Films Logged"));
        grid.add(createStatCard(String.valueOf(profile.filmsThisYear()), "Films This Year"));
        grid.add(createStatCard(String.valueOf(profile.listsCount()), "Curated Lists"));
        grid.add(createStatCard(String.valueOf(profile.reviewsWritten()), "Reviews Penned"));

        return grid;
    }

    private JPanel createStatCard(String value, String label) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(ThemeManager.BG_CARD);
        card.setBorder(ThemeManager.createCardBorder());

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(ThemeManager.FONT_TITLE);
        valueLabel.setForeground(ThemeManager.ACCENT_GOLD);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(label);
        descLabel.setFont(ThemeManager.FONT_SMALL);
        descLabel.setForeground(ThemeManager.TEXT_MUTED);
        descLabel.setAlignmentX(LEFT_ALIGNMENT);

        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(descLabel);

        return card;
    }

    private JPanel createFilmDnaSection(UserProfile profile) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_CARD);
        panel.setBorder(ThemeManager.createCardBorder());

        JLabel sectionTitle = new JLabel("Film DNA Taste Distribution");
        sectionTitle.setFont(ThemeManager.FONT_TITLE);
        sectionTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel sectionDesc = new JLabel("Calculated genre affinity breakdown based on your logged ratings and viewing velocity.");
        sectionDesc.setFont(ThemeManager.FONT_SMALL);
        sectionDesc.setForeground(ThemeManager.TEXT_MUTED);

        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sectionDesc);
        panel.add(Box.createVerticalStrut(14));

        // Embed the DnaBarChart reusable component
        DnaBarChart dnaChart = new DnaBarChart(profile.genrePercentages());
        panel.add(dnaChart);

        return panel;
    }

    private JPanel createDirectorsSection(UserProfile profile) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_CARD);
        panel.setBorder(ThemeManager.createCardBorder());

        JLabel sectionTitle = new JLabel("Favorite Auteurs & Directors");
        sectionTitle.setFont(ThemeManager.FONT_TITLE);
        sectionTitle.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel sectionDesc = new JLabel("Directors with the highest frequency and average score in your viewing history.");
        sectionDesc.setFont(ThemeManager.FONT_SMALL);
        sectionDesc.setForeground(ThemeManager.TEXT_MUTED);

        panel.add(sectionTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sectionDesc);
        panel.add(Box.createVerticalStrut(10));

        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        tagsPanel.setOpaque(false);

        for (String director : profile.favoriteDirectors()) {
            tagsPanel.add(BadgeLabel.createGenreBadge("🎬 " + director));
        }

        panel.add(tagsPanel);
        return panel;
    }
}
