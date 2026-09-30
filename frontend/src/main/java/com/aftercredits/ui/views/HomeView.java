package com.aftercredits.ui.views;

import com.aftercredits.model.Movie;
import com.aftercredits.model.UserProfile;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.MovieCard;
import com.aftercredits.ui.components.StarRatingBar;

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
import java.util.List;

/**
 * The primary dashboard screen for AfterCredits.
 * Features a welcoming header with quick stats, a spotlight feature film banner,
 * and a curated trending cinephile picks section using repository data.
 */
public class HomeView extends JPanel {

    private final CinemaRepository repository;

    public HomeView(CinemaRepository repository) {
        this.repository = repository;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        // Build the main scrollable content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ThemeManager.BG_PRIMARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        // 1. Welcome Header & Pulse Metrics
        contentPanel.add(createHeaderSection());
        contentPanel.add(Box.createVerticalStrut(20));

        // 2. Featured Spotlight Film Banner
        contentPanel.add(createSpotlightBanner());
        contentPanel.add(Box.createVerticalStrut(28));

        // 3. Trending Cinephile Picks Section
        contentPanel.add(createTrendingSection());

        // Wrap in a smooth scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderSection() {
        UserProfile profile = repository.getUserProfile();

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Greeting and subtitle
        JPanel titlesPanel = new JPanel();
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));
        titlesPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome back, " + profile.username() + " 👋");
        welcomeLabel.setFont(ThemeManager.FONT_TITLE_LARGE);
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

    private JPanel createSpotlightBanner() {
        Movie spotlight = repository.getSpotlightMovie();

        JPanel banner = new JPanel(new BorderLayout(20, 0));
        banner.setBackground(ThemeManager.BG_CARD);
        banner.setBorder(ThemeManager.createCardBorder());
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Left Accent Visual Block
        JPanel visualBlock = new JPanel(new BorderLayout());
        visualBlock.setPreferredSize(new Dimension(140, 150));
        visualBlock.setBackground(ThemeManager.BG_PRIMARY);
        visualBlock.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1, true));

        JLabel filmIcon = new JLabel("🎬", JLabel.CENTER);
        filmIcon.setFont(ThemeManager.FONT_TITLE_LARGE.deriveFont(38f));
        visualBlock.add(filmIcon, BorderLayout.CENTER);

        // Center Content Block
        JPanel infoBlock = new JPanel();
        infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
        infoBlock.setOpaque(false);

        // Tag row
        JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tagRow.setOpaque(false);
        tagRow.add(BadgeLabel.createAccentBadge("FEATURED SPOTLIGHT"));
        tagRow.add(new BadgeLabel(String.valueOf(spotlight.year())));
        tagRow.add(new BadgeLabel(spotlight.getFormattedDuration()));

        // Movie Title
        JLabel titleLabel = new JLabel(spotlight.title());
        titleLabel.setFont(ThemeManager.FONT_TITLE_LARGE);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        // Director
        JLabel directorLabel = new JLabel("Directed by " + spotlight.director() + " • " + spotlight.getGenresFormatted());
        directorLabel.setFont(ThemeManager.FONT_BODY);
        directorLabel.setForeground(ThemeManager.TEXT_MUTED);

        // Synopsis
        JLabel synopsisLabel = new JLabel("<html><body style='width: 520px;'>" + spotlight.synopsis() + "</body></html>");
        synopsisLabel.setFont(ThemeManager.FONT_BODY);
        synopsisLabel.setForeground(ThemeManager.TEXT_MUTED);

        // Star Rating
        StarRatingBar ratingBar = new StarRatingBar(spotlight.rating());

        infoBlock.add(tagRow);
        infoBlock.add(Box.createVerticalStrut(6));
        infoBlock.add(titleLabel);
        infoBlock.add(Box.createVerticalStrut(2));
        infoBlock.add(directorLabel);
        infoBlock.add(Box.createVerticalStrut(8));
        infoBlock.add(synopsisLabel);
        infoBlock.add(Box.createVerticalStrut(8));
        infoBlock.add(ratingBar);

        banner.add(visualBlock, BorderLayout.WEST);
        banner.add(infoBlock, BorderLayout.CENTER);

        return banner;
    }

    private JPanel createTrendingSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        // Section Title
        JLabel sectionTitle = new JLabel("Trending Cinephile Picks");
        sectionTitle.setFont(ThemeManager.FONT_TITLE);
        sectionTitle.setForeground(ThemeManager.TEXT_PRIMARY);
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(14));

        // Grid of Trending Movie Cards
        List<Movie> trending = repository.getTrendingMovies();
        int columns = Math.min(3, Math.max(1, trending.size()));
        JPanel grid = new JPanel(new GridLayout(0, columns, 14, 14));
        grid.setOpaque(false);

        for (Movie movie : trending) {
            grid.add(new MovieCard(movie));
        }

        section.add(grid);
        return section;
    }
}
