package com.aftercredits.ui.components;

import com.aftercredits.model.Movie;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.util.ScrollWheelForwarder;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * A reusable horizontal scrolling row of MovieCards.
 * Mouse-wheel events scroll the row horizontally. Vertical wheel events
 * that arrive when a parent vertical scroll pane is present are NOT
 * consumed here — they bubble up naturally.
 */
public class HorizontalMovieRow extends JPanel {

    public HorizontalMovieRow(String title, List<Movie> movies, Consumer<Movie> onSelectCallback) {
        setLayout(new BorderLayout(0, ThemeManager.SPACING_MD));
        setOpaque(false);
        setBackground(ThemeManager.BG_PRIMARY);
        // Row height: card height (310) + metadata below (42) + section header (40) + gaps
        setPreferredSize(new Dimension(800, 410));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 410));

        // 1. Section Header
        add(ThemeManager.createSectionHeader(title, "See All ›"), BorderLayout.NORTH);

        if (movies.isEmpty()) {
            JPanel empty = ThemeManager.createEmptyState("🎞️", "Nothing here yet",
                    "Explore and add films to see them here.");
            add(empty, BorderLayout.CENTER);
            return;
        }

        // 2. Card row panel
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, 0));
        rowPanel.setOpaque(false);
        for (Movie movie : movies) {
            rowPanel.add(new MovieCard(movie, onSelectCallback));
        }

        // 3. Horizontal-only scroll pane
        JScrollPane scrollPane = new JScrollPane(rowPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        // Horizontal wheel scroll on the scroll pane itself
        scrollPane.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar hbar = scrollPane.getHorizontalScrollBar();
                int move = e.getWheelRotation() * hbar.getUnitIncrement() * 3;
                hbar.setValue(hbar.getValue() + move);
                // Consume so the event doesn't also scroll the outer vertical pane
                e.consume();
            }
        });

        // Also install on child cards so wheel-over-card scrolls the row
        ScrollWheelForwarder.installOn(rowPanel, scrollPane);

        add(scrollPane, BorderLayout.CENTER);
    }
}
