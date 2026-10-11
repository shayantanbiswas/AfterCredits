package com.aftercredits.ui.views;

import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.MovieCard;
import com.aftercredits.ui.components.RoundedPanel;
import com.aftercredits.ui.util.ScrollWheelForwarder;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Modernized view for managing the user's personal cinema library.
 * Tabs: Watched, Watchlist, Diary, Collections.
 */
public class MyCinemaView extends JPanel {

    private final JTabbedPane tabbedPane;

    public MyCinemaView(CinemaRepository repository, Consumer<Movie> onMovieSelected) {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeManager.SPACING_LG, 0));

        JLabel title = new JLabel("My Cinema");
        title.setFont(ThemeManager.FONT_PAGE_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeManager.FONT_SECTION_HEADING);
        tabbedPane.setForeground(ThemeManager.TEXT_MUTED);
        tabbedPane.setBackground(ThemeManager.BG_PRIMARY);
        tabbedPane.setBorder(null);

        // Add tabs with text only
        tabbedPane.addTab("Watched",    createMovieGrid(repository.getWatchedMovies(), onMovieSelected));
        tabbedPane.addTab("Watchlist",  createMovieGrid(repository.getWatchlist(), onMovieSelected));
        tabbedPane.addTab("Diary",      createDiaryTab(repository));
        tabbedPane.addTab("Collections", createCollectionsTab(repository, onMovieSelected));
        
        // Guarantee icon rendering by using custom tab components instead of setIconAt
        tabbedPane.setTabComponentAt(0, createCustomTabComponent("Watched", new TabIcon(TabIcon.Type.WATCHED)));
        tabbedPane.setTabComponentAt(1, createCustomTabComponent("Watchlist", new TabIcon(TabIcon.Type.WATCHLIST)));
        tabbedPane.setTabComponentAt(2, createCustomTabComponent("Diary", new TabIcon(TabIcon.Type.DIARY)));
        tabbedPane.setTabComponentAt(3, createCustomTabComponent("Collections", new TabIcon(TabIcon.Type.COLLECTIONS)));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCustomTabComponent(String title, javax.swing.Icon icon) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(title);
        textLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        
        // Let the labels inherit the TabbedPane's foreground changes for selection states
        // In FlatLaf, custom tab components might not inherit colors automatically,
        // but we'll set them to inherit so they use the parent JTabbedPane's foreground.
        textLabel.setForeground(ThemeManager.TEXT_MUTED);

        panel.add(iconLabel);
        panel.add(textLabel);
        
        // Add a changelistener to the tabbed pane so the custom components can update their 
        // foreground color when selected.
        tabbedPane.addChangeListener(e -> {
            boolean isSelected = tabbedPane.indexOfTabComponent(panel) == tabbedPane.getSelectedIndex();
            textLabel.setForeground(isSelected ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            // The icon will redraw itself picking up the panel's foreground if we set it
            panel.setForeground(isSelected ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            iconLabel.setForeground(isSelected ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            panel.repaint();
        });

        // Initialize state
        panel.setForeground(ThemeManager.TEXT_MUTED);
        iconLabel.setForeground(ThemeManager.TEXT_MUTED);

        return panel;
    }

    /** Switches the visible tab based on the sidebar navigation key. */
    public void selectTab(String navKey) {
        switch (navKey) {
            case "WATCHED": tabbedPane.setSelectedIndex(0); break;
            case "WATCHLIST": tabbedPane.setSelectedIndex(1); break;
            case "DIARY": tabbedPane.setSelectedIndex(2); break;
            case "COLLECTIONS": tabbedPane.setSelectedIndex(3); break;
            case "MY_CINEMA": tabbedPane.setSelectedIndex(0); break;
        }
    }

    // ─── Watched / Watchlist grid ────────────────────────────────────────────

    private JPanel createMovieGrid(List<Movie> movies, Consumer<Movie> onMovieSelected) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_LG, 0, 0, 0));

        if (movies.isEmpty()) {
            panel.add(ThemeManager.createEmptyState("🎞️", "Your library is empty",
                    "Start exploring to add movies here."), BorderLayout.CENTER);
            return panel;
        }

        JPanel wrapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, ThemeManager.CARD_GAP));
        wrapPanel.setOpaque(false);
        for (Movie m : movies) {
            wrapPanel.add(new MovieCard(m, onMovieSelected));
        }
        panel.add(ThemeManager.createStyledScrollPane(wrapPanel), BorderLayout.CENTER);
        return panel;
    }

    // ─── Diary tab ───────────────────────────────────────────────────────────

    /**
     * Diary: derives entries from the user's watched movies paired with any matching
     * community reviews (acting as personal log entries when authored matches the
     * current user handle). Grouped by month for a chronological journal feel.
     */
    private JPanel createDiaryTab(CinemaRepository repository) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_LG, 0, 0, 0));

        List<Movie> watched = repository.getWatchedMovies();
        if (watched.isEmpty()) {
            panel.add(ThemeManager.createEmptyState("📔", "Your diary is empty",
                    "Movies you mark as watched will appear here as diary entries."), BorderLayout.CENTER);
            return panel;
        }

        // Group the watched list into month buckets using their release year as a surrogate date
        // (real diary dates would come from a log endpoint not yet in the repository interface)
        Map<String, List<Movie>> byYear = watched.stream()
                .collect(Collectors.groupingBy(m -> String.valueOf(m.year()),
                        Collectors.toList()));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        // Sort descending by year
        byYear.entrySet().stream()
                .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .forEach(entry -> {
                    // Month/year section header
                    JLabel periodLabel = new JLabel(entry.getKey());
                    periodLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
                    periodLabel.setForeground(ThemeManager.ACCENT_GOLD);
                    periodLabel.setAlignmentX(LEFT_ALIGNMENT);
                    periodLabel.setBorder(BorderFactory.createEmptyBorder(
                            ThemeManager.SPACING_MD, 0, ThemeManager.SPACING_SM, 0));
                    listPanel.add(periodLabel);
                    listPanel.add(ThemeManager.createDivider());
                    listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));

                    for (Movie m : entry.getValue()) {
                        listPanel.add(buildDiaryEntry(m));
                        listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));
                    }
                });

        panel.add(ThemeManager.createStyledScrollPane(listPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDiaryEntry(Movie movie) {
        RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        card.setLayout(new BorderLayout(ThemeManager.SPACING_MD, 0));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(LEFT_ALIGNMENT);

        // Date column
        JPanel dateCol = new JPanel();
        dateCol.setLayout(new BoxLayout(dateCol, BoxLayout.Y_AXIS));
        dateCol.setOpaque(false);
        dateCol.setPreferredSize(new Dimension(48, 60));

        JLabel dayLabel = new JLabel("—", JLabel.CENTER);
        dayLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        dayLabel.setForeground(ThemeManager.ACCENT_GOLD);
        dayLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel monthLabel = new JLabel(String.valueOf(movie.year()), JLabel.CENTER);
        monthLabel.setFont(ThemeManager.FONT_SECONDARY);
        monthLabel.setForeground(ThemeManager.TEXT_MUTED);
        monthLabel.setAlignmentX(CENTER_ALIGNMENT);

        dateCol.add(dayLabel);
        dateCol.add(monthLabel);

        // Movie info
        JPanel infoCol = new JPanel();
        infoCol.setLayout(new BoxLayout(infoCol, BoxLayout.Y_AXIS));
        infoCol.setOpaque(false);

        JLabel titleLabel = new JLabel(movie.title());
        titleLabel.setFont(ThemeManager.FONT_BUTTON);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(LEFT_ALIGNMENT);
        metaRow.add(BadgeLabel.createRatingBadge(movie.rating()));
        metaRow.add(new BadgeLabel(movie.getGenresFormatted()));

        infoCol.add(titleLabel);
        infoCol.add(Box.createVerticalStrut(6));
        infoCol.add(metaRow);

        card.add(dateCol, BorderLayout.WEST);
        card.add(infoCol, BorderLayout.CENTER);
        return card;
    }

    // ─── Collections tab ─────────────────────────────────────────────────────

    /**
     * Collections: groups the complete watched + watchlist library into automatic
     * genre-based collections (the only grouping axis available without a dedicated
     * collections repository method).
     */
    private JPanel createCollectionsTab(CinemaRepository repository, Consumer<Movie> onMovieSelected) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_LG, 0, 0, 0));

        List<Movie> allPersonal = new java.util.ArrayList<>();
        allPersonal.addAll(repository.getWatchedMovies());
        allPersonal.addAll(repository.getWatchlist());

        if (allPersonal.isEmpty()) {
            panel.add(ThemeManager.createEmptyState("📁", "No collections yet",
                    "Add movies to your watched list or watchlist to create automatic collections."), BorderLayout.CENTER);
            return panel;
        }

        // Build genre-based smart collections
        Map<String, List<Movie>> byGenre = allPersonal.stream()
                .filter(m -> !m.genres().isEmpty())
                .flatMap(m -> m.genres().stream().limit(2).map(g -> Map.entry(g, m)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        // Sort genre collections by size (largest first)
        byGenre.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .forEach(entry -> {
                    listPanel.add(buildCollectionSection(entry.getKey(), entry.getValue(), onMovieSelected));
                    listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));
                });

        panel.add(ThemeManager.createStyledScrollPane(listPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCollectionSection(String genre, List<Movie> movies, Consumer<Movie> onMovieSelected) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        // Section header: genre name + count badge
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.SPACING_SM, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel genreLabel = new JLabel(genre);
        genreLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        genreLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        headerRow.add(genreLabel);
        headerRow.add(new BadgeLabel(movies.size() + " films"));
        section.add(headerRow);
        section.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));

        // Horizontal row of cards (reuse HorizontalMovieRow approach inline)
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, 0));
        cardsRow.setOpaque(false);
        for (Movie m : movies) {
            cardsRow.add(new MovieCard(m, onMovieSelected));
        }

        javax.swing.JScrollPane rowScroll = new javax.swing.JScrollPane(cardsRow);
        rowScroll.setBorder(null);
        rowScroll.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        rowScroll.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        rowScroll.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rowScroll.getHorizontalScrollBar().setUnitIncrement(20);
        rowScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 330));
        rowScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        rowScroll.setAlignmentX(LEFT_ALIGNMENT);

        // Forward mouse wheel to horizontal scroll
        rowScroll.addMouseWheelListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                javax.swing.JScrollBar hbar = rowScroll.getHorizontalScrollBar();
                int move = e.getWheelRotation() * hbar.getUnitIncrement() * 3;
                hbar.setValue(hbar.getValue() + move);
                e.consume();
            }
        });
        ScrollWheelForwarder.installOn(cardsRow, rowScroll);

        section.add(rowScroll);
        return section;
    }

    /**
     * Programmatic vector icons for the My Cinema tabs to match the dark cinematic theme
     * and avoid missing Unicode character fallbacks.
     */
    private static class TabIcon implements javax.swing.Icon {
        enum Type { WATCHED, WATCHLIST, DIARY, COLLECTIONS }

        private final Type type;
        private final int size = 16;

        TabIcon(Type type) {
            this.type = type;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw using the component's foreground (which changes on hover/selection in FlatLaf)
            g2.setColor(c.getForeground());
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

            int cx = x + size / 2;
            int cy = y + size / 2;

            switch (type) {
                case WATCHED:
                    // Eye shape
                    g2.drawArc(x + 1, y + 4, size - 2, size - 8, 0, 180);
                    g2.drawArc(x + 1, y + 4, size - 2, size - 8, 180, 180);
                    g2.drawOval(cx - 2, cy - 2, 4, 4);
                    break;
                case WATCHLIST:
                    // Bookmark shape
                    java.awt.geom.GeneralPath bookmark = new java.awt.geom.GeneralPath();
                    bookmark.moveTo(x + 4, y + 2);
                    bookmark.lineTo(x + size - 4, y + 2);
                    bookmark.lineTo(x + size - 4, y + size - 2);
                    bookmark.lineTo(cx, y + size - 5);
                    bookmark.lineTo(x + 4, y + size - 2);
                    bookmark.closePath();
                    g2.draw(bookmark);
                    break;
                case DIARY:
                    // Calendar shape
                    g2.drawRoundRect(x + 2, y + 3, size - 4, size - 5, 2, 2);
                    g2.drawLine(x + 2, y + 7, x + size - 2, y + 7);
                    g2.drawLine(x + 5, y + 1, x + 5, y + 3);
                    g2.drawLine(x + size - 5, y + 1, x + size - 5, y + 3);
                    break;
                case COLLECTIONS:
                    // Grid/Folder shape - 4 small squares
                    g2.drawRect(x + 2, y + 2, 5, 5);
                    g2.drawRect(x + 9, y + 2, 5, 5);
                    g2.drawRect(x + 2, y + 9, 5, 5);
                    g2.drawRect(x + 9, y + 9, 5, 5);
                    break;
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }
    }
}
