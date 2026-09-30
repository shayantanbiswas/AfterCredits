package com.aftercredits.ui.components;

import com.aftercredits.theme.ThemeManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reusable star rating component.
 * Supports both read-only display mode (for movie cards and reviews) and
 * interactive mode (where clicking a star updates the rating value and notifies listeners).
 */
public class StarRatingBar extends JPanel {

    private static final int MAX_STARS = 5;
    private static final Color STAR_FILLED = ThemeManager.ACCENT_GOLD;
    private static final Color STAR_EMPTY = new Color(55, 65, 80);

    private double currentRating;
    private final boolean interactive;
    private Consumer<Double> onRatingChangedListener;

    private final List<JLabel> starLabels = new ArrayList<>();
    private final JLabel scoreLabel = new JLabel();

    /**
     * Creates a read-only star rating display.
     *
     * @param rating score on a 5.0 scale (or 10.0 scale, automatically normalized if > 5.0)
     */
    public StarRatingBar(double rating) {
        this(rating, false, null);
    }

    /**
     * Full constructor supporting both display and interactive modes.
     *
     * @param initialRating           initial score value
     * @param interactive             true if user can click to change rating
     * @param onRatingChangedListener callback invoked with the new rating on click
     */
    public StarRatingBar(double initialRating, boolean interactive, Consumer<Double> onRatingChangedListener) {
        this.interactive = interactive;
        this.onRatingChangedListener = onRatingChangedListener;
        this.currentRating = normalizeRating(initialRating);

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        buildStarComponents();
        updateVisualStars(this.currentRating);
    }

    private double normalizeRating(double rating) {
        // If a rating is on a 10-point scale (like 8.5), normalize to a 5-star scale (4.25)
        if (rating > 5.0) {
            return rating / 2.0;
        }
        return Math.max(0.0, Math.min(5.0, rating));
    }

    private void buildStarComponents() {
        for (int i = 1; i <= MAX_STARS; i++) {
            final int starIndex = i;
            JLabel star = new JLabel("★");
            star.setFont(ThemeManager.FONT_BODY_BOLD);
            star.setForeground(STAR_EMPTY);

            if (interactive) {
                star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                star.setToolTipText("Rate " + starIndex + " out of " + MAX_STARS);

                star.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        // Hover preview: temporarily illuminate stars up to this index
                        updateVisualStars(starIndex);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        // Restore confirmed rating
                        updateVisualStars(currentRating);
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Confirm rating selection
                        setRating(starIndex);
                        if (onRatingChangedListener != null) {
                            onRatingChangedListener.accept((double) starIndex);
                        }
                    }
                });
            }

            starLabels.add(star);
            add(star);
            add(Box.createHorizontalStrut(2));
        }

        // Numeric score label
        add(Box.createHorizontalStrut(6));
        scoreLabel.setFont(ThemeManager.FONT_SMALL);
        scoreLabel.setForeground(ThemeManager.TEXT_MUTED);
        add(scoreLabel);
    }

    /**
     * Updates star colors based on the given rating value.
     */
    private void updateVisualStars(double ratingValue) {
        for (int i = 0; i < MAX_STARS; i++) {
            JLabel star = starLabels.get(i);
            if (ratingValue >= (i + 1)) {
                star.setForeground(STAR_FILLED);
            } else if (ratingValue > i && ratingValue < (i + 1)) {
                // Partial fill approximation
                star.setForeground(ThemeManager.ACCENT_GOLD_MUTED);
            } else {
                star.setForeground(STAR_EMPTY);
            }
        }

        if (interactive) {
            scoreLabel.setText(String.format("%.1f / 5", ratingValue));
        } else {
            scoreLabel.setText(String.format("%.1f", ratingValue));
        }
    }

    /**
     * Programmatically sets the rating and updates the display.
     */
    public void setRating(double newRating) {
        this.currentRating = normalizeRating(newRating);
        updateVisualStars(this.currentRating);
        repaint();
    }

    public double getRating() {
        return currentRating;
    }

    public void setOnRatingChangedListener(Consumer<Double> listener) {
        this.onRatingChangedListener = listener;
    }
}
