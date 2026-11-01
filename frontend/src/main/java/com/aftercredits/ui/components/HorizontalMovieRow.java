
package com.aftercredits.ui.components;

import com.aftercredits.model.Movie;
import com.aftercredits.theme.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class HorizontalMovieRow extends JPanel {

    private static final int CARD_STEP = 214;
    private static final int ANIMATION_STEP = 24;
    private static final int ANIMATION_DELAY = 12;

    private final JScrollPane scrollPane;
    private final JPanel rowPanel;
    private final Timer animationTimer;

    public HorizontalMovieRow(
            String title,
            List<Movie> movies,
            Consumer<Movie> onSelectCallback) {

        setLayout(new BorderLayout(0, ThemeManager.SPACING_MD));
        setOpaque(false);
        setBackground(ThemeManager.BG_PRIMARY);

        setPreferredSize(new Dimension(800, 380));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        add(
                ThemeManager.createSectionHeader(title, "See All ›"),
                BorderLayout.NORTH
        );

        if (movies == null || movies.isEmpty()) {
            add(
                    ThemeManager.createEmptyState(
                            "🎞️",
                            "Nothing here yet",
                            "Explore and add films to see them here."
                    ),
                    BorderLayout.CENTER
            );
            animationTimer = null;
            scrollPane = null;
            rowPanel = null;
            return;
        }

        rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setOpaque(false);

        for (Movie movie : movies) {
            rowPanel.add(new MovieCard(movie, onSelectCallback));
            rowPanel.add(Box.createHorizontalStrut(ThemeManager.CARD_GAP));
        }

        scrollPane = new JScrollPane(rowPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        );
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        // Prevent Swing's wheel scrolling from fighting the carousel.
        scrollPane.addMouseWheelListener(e -> e.consume());
        rowPanel.addMouseWheelListener(e -> e.consume());

        animationTimer = new Timer(ANIMATION_DELAY, e -> animateScroll());

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setOpaque(false);

        layeredPane.setLayout(null);

        layeredPane.add(scrollPane, JLayeredPane.DEFAULT_LAYER);

        JButton left = createArrowButton("‹");
        JButton right = createArrowButton("›");

        layeredPane.add(left, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(right, JLayeredPane.PALETTE_LAYER);

        left.addActionListener(e -> scrollBy(-CARD_STEP));
        right.addActionListener(e -> scrollBy(CARD_STEP));

        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutCarousel(layeredPane, left, right);
            }
        });

        add(layeredPane, BorderLayout.CENTER);
    }

    private JButton createArrowButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 28));
        button.setForeground(ThemeManager.TEXT_PRIMARY);
        button.setBackground(new Color(15, 18, 24, 190));
        button.setOpaque(true);
        button.setBorder(
                BorderFactory.createLineBorder(
                        new Color(255, 255, 255, 35)
                )
        );
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(229, 169, 60, 210));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(15, 18, 24, 190));
            }
        });

        return button;
    }

    private void layoutCarousel(
            JLayeredPane layeredPane,
            JButton left,
            JButton right) {

        int width = layeredPane.getWidth();
        int height = layeredPane.getHeight();

        scrollPane.setBounds(0, 0, width, height);

        int buttonWidth = 42;
        int buttonHeight = 64;
        int y = Math.max(0, (height - buttonHeight) / 2);

        left.setBounds(8, y, buttonWidth, buttonHeight);
        right.setBounds(
                Math.max(8, width - buttonWidth - 8),
                y,
                buttonWidth,
                buttonHeight
        );
    }

    private void scrollBy(int amount) {
        if (scrollPane == null) {
            return;
        }

        int current = scrollPane.getHorizontalScrollBar().getValue();
        int target = current + amount;

        JScrollBar bar = scrollPane.getHorizontalScrollBar();

        int max =
                Math.max(
                        bar.getMinimum(),
                        bar.getMaximum() - bar.getVisibleAmount()
                );

        target = Math.max(bar.getMinimum(), Math.min(target, max));

        animationTarget = target;

        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    private int animationTarget = 0;

    private void animateScroll() {
        if (scrollPane == null) {
            animationTimer.stop();
            return;
        }

        JScrollBar bar = scrollPane.getHorizontalScrollBar();
        int current = bar.getValue();

        if (current == animationTarget) {
            animationTimer.stop();
            return;
        }

        int difference = animationTarget - current;
        int movement =
                Math.min(
                        ANIMATION_STEP,
                        Math.max(1, Math.abs(difference) / 3)
                );

        bar.setValue(
                current + (difference > 0 ? movement : -movement)
        );
    }
}
