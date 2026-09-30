package com.aftercredits.ui.views;

import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.MovieCard;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Discover screen for AfterCredits.
 * Supports live international and classic cinema search via TMDB API.
 * Uses a debounce timer and SwingWorker to guarantee that network requests NEVER
 * execute on the Swing Event Dispatch Thread (EDT).
 * Employs monotonic sequence IDs to prevent older search results from overwriting newer ones.
 */
public class DiscoverView extends JPanel {

    private static final String[] GENRE_OPTIONS = {
            "All", "Sci-Fi", "Drama", "Thriller", "Adventure", "Animation", "Comedy", "Romance", "Crime", "Mystery", "Action"
    };

    private final CinemaRepository repository;
    private final JTextField searchField;
    private final JComboBox<String> genreComboBox;
    private final JLabel resultsCountLabel;
    private final JPanel cardsGrid;
    private final JPanel emptyStatePanel;

    // Concurrency & Debounce controls
    private final Timer searchDebounceTimer;
    private final AtomicInteger searchSequence = new AtomicInteger(0);

    public DiscoverView(CinemaRepository repository) {
        this.repository = repository;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);

        // Main scrollable container
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(ThemeManager.BG_PRIMARY);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        // 1. Screen Title & Subtitle with TMDB Attribution
        contentPanel.add(createHeaderPanel());
        contentPanel.add(Box.createVerticalStrut(18));

        // 2. Interactive Filter & Search Controls Bar
        this.searchField = new JTextField(16);
        this.genreComboBox = new JComboBox<>(GENRE_OPTIONS);
        this.resultsCountLabel = new JLabel("Initialising catalogue...");
        contentPanel.add(createFilterToolbar());
        contentPanel.add(Box.createVerticalStrut(16));

        // 3. Movie Grid Container & Empty State
        this.cardsGrid = new JPanel(new GridLayout(0, 3, 14, 14));
        this.cardsGrid.setOpaque(false);

        this.emptyStatePanel = createEmptyStatePanel();
        this.emptyStatePanel.setVisible(false);

        contentPanel.add(emptyStatePanel);
        contentPanel.add(cardsGrid);

        // 4. Configure Debounce Timer (350ms delay for smooth typing without API flooding)
        this.searchDebounceTimer = new Timer(350, e -> triggerAsyncSearch());
        this.searchDebounceTimer.setRepeats(false);

        // Hook up live dynamic filtering
        setupEventHandlers();

        // Initial catalogue load on background thread
        triggerAsyncSearch();

        // Wrap in smooth scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel title = new JLabel("Discover Cinema");
        title.setFont(ThemeManager.FONT_TITLE_LARGE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Search global cinema across genres, eras, and auteurs (including Satyajit Ray, Hitchcock, and modern releases).");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);

        JLabel attribution = new JLabel("Metadata & posters provided by TMDB. This product is not certified or endorsed by TMDB.");
        attribution.setFont(ThemeManager.FONT_SMALL);
        attribution.setForeground(new Color(110, 125, 145));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(2));
        panel.add(attribution);
        return panel;
    }

    private JPanel createFilterToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setBackground(ThemeManager.BG_CARD);
        toolbar.setBorder(ThemeManager.createCardBorder());
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        // Left: Search Field & Genre Dropdown
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        searchField.putClientProperty("JTextField.placeholderText", "Search by film title or director...");
        searchField.setPreferredSize(new Dimension(240, 32));

        JLabel genreLabel = new JLabel("Genre:");
        genreLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        genreLabel.setForeground(ThemeManager.TEXT_MUTED);

        genreComboBox.setPreferredSize(new Dimension(130, 32));

        JButton clearBtn = new JButton("Reset");
        clearBtn.setFont(ThemeManager.FONT_SMALL);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            genreComboBox.setSelectedIndex(0);
            triggerAsyncSearch();
        });

        controls.add(searchIcon);
        controls.add(searchField);
        controls.add(Box.createHorizontalStrut(8));
        controls.add(genreLabel);
        controls.add(genreComboBox);
        controls.add(clearBtn);

        // Right: Active Result Counter & Mode Indicator
        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        countPanel.setOpaque(false);
        resultsCountLabel.setFont(ThemeManager.FONT_SMALL);
        resultsCountLabel.setForeground(ThemeManager.TEXT_MUTED);
        countPanel.add(resultsCountLabel);

        toolbar.add(controls, BorderLayout.WEST);
        toolbar.add(countPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));

        JLabel icon = new JLabel("🎞️", JLabel.CENTER);
        icon.setFont(ThemeManager.FONT_TITLE_LARGE.deriveFont(32f));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel message = new JLabel("No films found matching your search criteria.");
        message.setFont(ThemeManager.FONT_BODY_BOLD);
        message.setForeground(ThemeManager.TEXT_PRIMARY);
        message.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Try searching with an English or original title (e.g. 'Pather Panchali', 'Vertigo', 'Dune').");
        hint.setFont(ThemeManager.FONT_SMALL);
        hint.setForeground(ThemeManager.TEXT_MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(8));
        panel.add(message);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hint);

        return panel;
    }

    private void setupEventHandlers() {
        // Debounce text field changes to prevent hammering the network
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleSearch();
            }
        });

        // Genre dropdown changes trigger immediate search
        genreComboBox.addActionListener(e -> triggerAsyncSearch());
    }

    private void scheduleSearch() {
        resultsCountLabel.setText("Typing...");
        searchDebounceTimer.restart();
    }

    /**
     * Executes repository search on a background worker thread.
     * Prevents EDT freezing and discards out-of-order responses.
     */
    private void triggerAsyncSearch() {
        searchDebounceTimer.stop();

        final String query = searchField.getText();
        final String selectedGenre = (String) genreComboBox.getSelectedItem();
        final int currentSeq = searchSequence.incrementAndGet();

        resultsCountLabel.setText("Searching catalogue...");

        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() {
                // Background execution (off the EDT!)
                return repository.searchMovies(query, selectedGenre);
            }

            @Override
            protected void done() {
                // Back on the Swing Event Dispatch Thread (EDT)
                if (currentSeq != searchSequence.get() || isCancelled()) {
                    // An older request finished after a newer one; discard obsolete results
                    return;
                }

                try {
                    List<Movie> matchedMovies = get();
                    cardsGrid.removeAll();

                    if (matchedMovies.isEmpty()) {
                        emptyStatePanel.setVisible(true);
                        cardsGrid.setVisible(false);
                        resultsCountLabel.setText("0 films found");
                    } else {
                        emptyStatePanel.setVisible(false);
                        cardsGrid.setVisible(true);

                        boolean isLive = matchedMovies.stream().anyMatch(m -> !m.isMock());
                        String sourceTag = isLive ? "TMDB Live" : "Offline Demo";
                        resultsCountLabel.setText("Showing " + matchedMovies.size() + " films (" + sourceTag + ")");

                        for (Movie movie : matchedMovies) {
                            cardsGrid.add(new MovieCard(movie));
                        }
                    }

                    cardsGrid.revalidate();
                    cardsGrid.repaint();
                } catch (Exception e) {
                    resultsCountLabel.setText("⚠️ Search error. Using offline fallback.");
                }
            }
        };

        worker.execute();
    }
}
