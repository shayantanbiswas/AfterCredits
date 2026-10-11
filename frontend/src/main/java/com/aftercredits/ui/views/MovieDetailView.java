package com.aftercredits.ui.views;

import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.util.AsyncImageLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Dedicated view for displaying full details of a specific movie.
 */
public class MovieDetailView extends JPanel {

    public MovieDetailView(Movie movie, CinemaRepository repository, Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(ThemeManager.BG_PRIMARY);

        // Top action bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        topBar.setOpaque(false);
        JButton backBtn = ThemeManager.createGhostButton("← Back");
        backBtn.addActionListener(e -> onBack.run());
        topBar.add(backBtn);
        content.add(topBar);

        // Main details layout
        JPanel splitPanel = new JPanel(new BorderLayout(40, 0));
        splitPanel.setOpaque(false);
        splitPanel.setBorder(ThemeManager.createViewPadding());

        // Left: High-res Poster
        JLabel posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(300, 450));
        AsyncImageLoader.loadImage(movie.getPosterUrl("w500"), 300, 450, movie.title(), posterLabel::setIcon);
        splitPanel.add(posterLabel, BorderLayout.WEST);

        // Right: Information and Actions
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(movie.title());
        titleLabel.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(36f));
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(LEFT_ALIGNMENT);
        metaRow.add(BadgeLabel.createYearBadge(movie.year()));
        metaRow.add(BadgeLabel.createRatingBadge(movie.rating()));
        metaRow.add(new BadgeLabel(movie.getFormattedDuration()));
        metaRow.add(new BadgeLabel(movie.getGenresFormatted()));

        JLabel directorLabel = new JLabel("Directed by " + movie.director());
        directorLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        directorLabel.setForeground(ThemeManager.TEXT_MUTED);
        directorLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel synopsisLabel = new JLabel("<html><body style='width: 500px; line-height: 1.4;'>" + movie.synopsis() + "</body></html>");
        synopsisLabel.setFont(ThemeManager.FONT_BODY);
        synopsisLabel.setForeground(ThemeManager.TEXT_MUTED);
        synopsisLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel castLabel = new JLabel("<html><body style='width: 500px;'><b>Cast:</b> " + movie.getCastFormatted() + "</body></html>");
        castLabel.setFont(ThemeManager.FONT_BODY);
        castLabel.setForeground(ThemeManager.TEXT_MUTED);
        castLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionsRow.setOpaque(false);
        actionsRow.setAlignmentX(LEFT_ALIGNMENT);
        actionsRow.add(ThemeManager.createAccentButton("▶ Play"));
        actionsRow.add(ThemeManager.createGhostButton("+ Watchlist"));
        actionsRow.add(ThemeManager.createGhostButton("✓ Watched"));

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(metaRow);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(directorLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(synopsisLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(castLabel);
        infoPanel.add(Box.createVerticalStrut(30));
        infoPanel.add(actionsRow);

        splitPanel.add(infoPanel, BorderLayout.CENTER);
        content.add(splitPanel);

        add(ThemeManager.createStyledScrollPane(content), BorderLayout.CENTER);
    }
}
