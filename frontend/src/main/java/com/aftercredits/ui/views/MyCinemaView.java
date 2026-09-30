package com.aftercredits.ui.views;

import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.MovieCard;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

/**
 * The personal cinema library screen for AfterCredits.
 * Organizes personal film tracking into four tabs:
 * 1. Watched (grid of logged films)
 * 2. Watchlist (queued films)
 * 3. Diary (chronological tabular log)
 * 4. Collections (prototype empty state pending Milestone 2 backend schema)
 */
public class MyCinemaView extends JPanel {

    private final CinemaRepository repository;

    public MyCinemaView(CinemaRepository repository) {
        this.repository = repository;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);

        // JTabbedPane containing the 4 personal cinema sections
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(ThemeManager.BG_PRIMARY);
        tabbedPane.setFont(ThemeManager.FONT_BODY_BOLD);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 24, 20, 24));

        tabbedPane.addTab("Watched (" + repository.getWatchedMovies().size() + ")", createWatchedTab());
        tabbedPane.addTab("Watchlist (" + repository.getWatchlist().size() + ")", createWatchlistTab());
        tabbedPane.addTab("Diary Log", createDiaryTab());
        tabbedPane.addTab("Collections", createCollectionsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 8, 28));

        JLabel title = new JLabel("My Cinema");
        title.setFont(ThemeManager.FONT_TITLE_LARGE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage your logged films, personal watchlist, viewing diary, and custom lists.");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        return panel;
    }

    private JScrollPane createWatchedTab() {
        List<Movie> watched = repository.getWatchedMovies();
        return createMovieGridScrollPane(watched, "No watched films logged yet.", "Log films from the Discover screen to build your history.");
    }

    private JScrollPane createWatchlistTab() {
        List<Movie> watchlist = repository.getWatchlist();
        return createMovieGridScrollPane(watchlist, "Your watchlist is empty.", "Browse the Discover screen to save films you want to watch.");
    }

    private JScrollPane createMovieGridScrollPane(List<Movie> movies, String emptyTitle, String emptyHint) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(ThemeManager.BG_PRIMARY);
        container.setBorder(BorderFactory.createEmptyBorder(16, 8, 20, 8));

        if (movies.isEmpty()) {
            container.add(createEmptyState(emptyTitle, emptyHint));
        } else {
            JPanel grid = new JPanel(new GridLayout(0, 3, 14, 14));
            grid.setOpaque(false);
            for (Movie movie : movies) {
                grid.add(new MovieCard(movie));
            }
            container.add(grid);
        }

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createDiaryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 8, 20, 8));

        List<Movie> watched = repository.getWatchedMovies();

        // Table Model using existing Movie data
        String[] columnNames = {"#", "Film Title", "Year", "Director", "Genres", "Community Rating"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only diary display
            }
        };

        int index = 1;
        for (Movie m : watched) {
            tableModel.addRow(new Object[]{
                    index++,
                    m.title(),
                    m.year(),
                    m.director(),
                    m.getGenresFormatted(),
                    "★ " + m.rating()
            });
        }

        JTable diaryTable = new JTable(tableModel);
        diaryTable.setFont(ThemeManager.FONT_BODY);
        diaryTable.setRowHeight(32);
        diaryTable.setShowGrid(false);
        diaryTable.setIntercellSpacing(new Dimension(0, 4));
        diaryTable.getTableHeader().setFont(ThemeManager.FONT_BODY_BOLD);
        diaryTable.getTableHeader().setBackground(ThemeManager.BG_CARD);
        diaryTable.getTableHeader().setForeground(ThemeManager.TEXT_PRIMARY);

        // Adjust column widths
        diaryTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        diaryTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        diaryTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        diaryTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        diaryTable.getColumnModel().getColumn(4).setPreferredWidth(240);
        diaryTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        JScrollPane tableScroll = new JScrollPane(diaryTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1));
        tableScroll.getViewport().setBackground(ThemeManager.BG_PRIMARY);

        panel.add(tableScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCollectionsTab() {
        // Honest empty/prototype state for Collections feature
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(36, 16, 36, 16));

        JLabel icon = new JLabel("📁", JLabel.CENTER);
        icon.setFont(ThemeManager.FONT_TITLE_LARGE.deriveFont(36f));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Curated Collections (Milestone 2 Feature)");
        heading.setFont(ThemeManager.FONT_TITLE);
        heading.setForeground(ThemeManager.TEXT_PRIMARY);
        heading.setAlignmentX(CENTER_ALIGNMENT);

        JLabel description = new JLabel("<html><center style='width: 480px;'>"
                + "Custom user-curated lists (e.g. <i>'Atmospheric Neo-Noir'</i>, <i>'Criterion 4K Restorations'</i>) "
                + "will be fully persistent in Milestone 2 when the Spring Boot and MySQL relational schema are connected."
                + "</center></html>");
        description.setFont(ThemeManager.FONT_BODY);
        description.setForeground(ThemeManager.TEXT_MUTED);
        description.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(10));
        panel.add(heading);
        panel.add(Box.createVerticalStrut(6));
        panel.add(description);

        return panel;
    }

    private JPanel createEmptyState(String title, String hint) {
        JPanel empty = new JPanel();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setOpaque(false);
        empty.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

        JLabel icon = new JLabel("🎬", JLabel.CENTER);
        icon.setFont(ThemeManager.FONT_TITLE_LARGE.deriveFont(32f));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(ThemeManager.FONT_SMALL);
        hintLabel.setForeground(ThemeManager.TEXT_MUTED);
        hintLabel.setAlignmentX(CENTER_ALIGNMENT);

        empty.add(icon);
        empty.add(Box.createVerticalStrut(8));
        empty.add(titleLabel);
        empty.add(Box.createVerticalStrut(4));
        empty.add(hintLabel);

        return empty;
    }
}
