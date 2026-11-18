package com.aftercredits.ui.util;

import com.aftercredits.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.net.URI;

/**
 * Utility for launching movie trailer playback.
 * Opens the trailer URL in the default web browser, with a fallback dialog if needed.
 */
public class TrailerPlayer {

    private TrailerPlayer() {}

    /**
     * Plays or opens the movie trailer URL in the user's default browser.
     *
     * @param parent     Parent component for centering fallback dialogs
     * @param trailerUrl Direct YouTube or search URL for the trailer
     * @param movieTitle Name of the movie
     */
    public static void openTrailer(Component parent, String trailerUrl, String movieTitle) {
        if (trailerUrl == null || trailerUrl.isBlank()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "No trailer link available for " + (movieTitle != null ? movieTitle : "this movie") + ".",
                    "Trailer Unavailable",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(trailerUrl));
                return;
            }
        } catch (Exception ignored) {
            // Fall through to fallback dialog
        }

        showFallbackTrailerDialog(parent, trailerUrl, movieTitle);
    }

    private static void showFallbackTrailerDialog(Component parent, String trailerUrl, String movieTitle) {
        JDialog dialog = new JDialog(
                parent == null ? null : SwingUtilities.getWindowAncestor(parent),
                "Movie Trailer — " + (movieTitle != null ? movieTitle : "AfterCredits"),
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(480, 240);
        dialog.setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titleLabel = new JLabel("Official Trailer");
        titleLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        titleLabel.setForeground(ThemeManager.ACCENT_GOLD);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel = new JLabel("<html><center>Click below to open the trailer or copy the link:</center></html>");
        infoLabel.setFont(ThemeManager.FONT_BODY);
        infoLabel.setForeground(ThemeManager.TEXT_MUTED);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField urlField = new JTextField(trailerUrl);
        urlField.setEditable(false);
        urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        urlField.setBackground(ThemeManager.BG_INPUT);
        urlField.setForeground(ThemeManager.TEXT_PRIMARY);
        urlField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR),
                new EmptyBorder(0, 10, 0, 10)
        ));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton openBtn = ThemeManager.createAccentButton("Open in Browser");
        openBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(trailerUrl));
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Could not open browser automatically. Please copy the link.");
            }
        });

        JButton copyBtn = ThemeManager.createGhostButton("Copy Link");
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(trailerUrl), null);
            JOptionPane.showMessageDialog(dialog, "Trailer link copied to clipboard!");
        });

        buttons.add(openBtn);
        buttons.add(copyBtn);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(infoLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(urlField);
        panel.add(Box.createVerticalStrut(18));
        panel.add(buttons);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}

