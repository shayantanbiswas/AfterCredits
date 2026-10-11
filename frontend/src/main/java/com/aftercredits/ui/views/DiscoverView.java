package com.aftercredits.ui.views;

import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.MovieCard;
import com.aftercredits.ui.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Modern discover view featuring debounced asynchronous search and responsive grid.
 */
public class DiscoverView extends JPanel {

    private final CinemaRepository repository;
    private final Consumer<Movie> onMovieSelected;
    private final AtomicInteger searchSequence = new AtomicInteger(0);

    private JTextField searchField;
    private JPanel gridPanel;
    private JScrollPane scrollPane;

    public DiscoverView(CinemaRepository repository, Consumer<Movie> onMovieSelected) {
        this.repository = repository;
        this.onMovieSelected = onMovieSelected;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        // Header and Search Bar
        add(createSearchHeader(), BorderLayout.NORTH);

        // Responsive Grid Area
        gridPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, ThemeManager.CARD_GAP));
        gridPanel.setOpaque(false);
        
        scrollPane = ThemeManager.createStyledScrollPane(gridPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Add component listener to revalidate grid on window resize so FlowLayout re-wraps
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gridPanel.revalidate();
                gridPanel.repaint();
            }
        });

        // Trigger initial empty search
        triggerAsyncSearch();
    }

    private JPanel createSearchHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Discover");
        title.setFont(ThemeManager.FONT_PAGE_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        RoundedPanel searchContainer = new RoundedPanel(ThemeManager.BG_INPUT, 20, ThemeManager.BORDER_COLOR);
        searchContainer.setLayout(new BorderLayout(10, 0));
        searchContainer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchContainer.setPreferredSize(new Dimension(300, 40));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setForeground(ThemeManager.TEXT_MUTED);
        
        searchField = new JTextField();
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setForeground(ThemeManager.TEXT_PRIMARY);
        searchField.setCaretColor(ThemeManager.ACCENT_GOLD);
        searchField.setFont(ThemeManager.FONT_BODY);
        
        Timer debounceTimer = new Timer(350, e -> triggerAsyncSearch());
        debounceTimer.setRepeats(false);

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                debounceTimer.restart();
            }
        });

        searchContainer.add(searchIcon, BorderLayout.WEST);
        searchContainer.add(searchField, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(searchContainer);

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private void triggerAsyncSearch() {
        String query = searchField.getText();
        final int currentSeq = searchSequence.incrementAndGet();

        gridPanel.removeAll();
        gridPanel.add(createLoadingState());
        gridPanel.revalidate();
        gridPanel.repaint();

        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() {
                return repository.searchMovies(query, "All");
            }

            @Override
            protected void done() {
                if (currentSeq != searchSequence.get()) return;

                try {
                    List<Movie> results = get();
                    gridPanel.removeAll();

                    if (results.isEmpty()) {
                        gridPanel.add(ThemeManager.createEmptyState("🤷", "No movies found", "Try a different search term."));
                    } else {
                        for (Movie m : results) {
                            gridPanel.add(new MovieCard(m, onMovieSelected));
                        }
                    }

                    // Reset any previously forced size so FlowLayout can compute the correct wrap height
                    gridPanel.setPreferredSize(null);
                    gridPanel.revalidate();
                    gridPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    /**
     * Creates a visually polished loading indicator consistent with ThemeManager's
     * empty-state style: centred icon, bold status line, and muted hint.
     */
    private JPanel createLoadingState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(80, 0, 80, 0));

        // Animated-feel spinner using a rotating clapperboard emoji
        JLabel spinnerLabel = new JLabel("🎬", JLabel.CENTER);
        spinnerLabel.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(40f));
        spinnerLabel.setForeground(ThemeManager.ACCENT_GOLD);
        spinnerLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("Searching…");
        statusLabel.setFont(ThemeManager.FONT_BUTTON);
        statusLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("Fetching results from your cinema library");
        hintLabel.setFont(ThemeManager.FONT_SECONDARY);
        hintLabel.setForeground(ThemeManager.TEXT_MUTED);
        hintLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Animated progress bar using a Swing Timer to cycle dots
        JLabel dotsLabel = new JLabel("●  ●  ●");
        dotsLabel.setFont(ThemeManager.FONT_SECONDARY);
        dotsLabel.setForeground(ThemeManager.TEXT_FAINT);
        dotsLabel.setAlignmentX(CENTER_ALIGNMENT);

        String[] dotFrames = {"●  ○  ○", "○  ●  ○", "○  ○  ●", "○  ●  ○"};
        int[] frameIdx = {0};
        Timer dotTimer = new Timer(300, e -> {
            dotsLabel.setText(dotFrames[frameIdx[0] % dotFrames.length]);
            frameIdx[0]++;
        });
        dotTimer.start();

        // Stop the timer when this panel is no longer showing (prevents leaks)
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override public void ancestorAdded(javax.swing.event.AncestorEvent e) { dotTimer.start(); }
            @Override public void ancestorRemoved(javax.swing.event.AncestorEvent e) { dotTimer.stop(); }
            @Override public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });

        panel.add(spinnerLabel);
        panel.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(ThemeManager.SPACING_XS));
        panel.add(hintLabel);
        panel.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));
        panel.add(dotsLabel);

        return panel;
    }
}
