package com.aftercredits.ui.views;

import java.util.function.Consumer;
import com.aftercredits.model.Movie;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Settings view for configuring application preferences and API connectivity.
 * Routes from Sidebar → ACCOUNT → Settings.
 */
public class SettingsView extends JPanel {
    private final CinemaRepository repository;
    private final Consumer<String> onThemeChange;
    private final Runnable onSessionUpdated;
    private JPanel content;

    public SettingsView(CinemaRepository repository, Consumer<String> onThemeChange) {
        this(repository, onThemeChange, null);
    }

    public SettingsView(CinemaRepository repository, Consumer<String> onThemeChange, Runnable onSessionUpdated) {
        this.repository = repository;
        this.onThemeChange = onThemeChange;
        this.onSessionUpdated = onSessionUpdated;
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        // Page header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeManager.SPACING_XL, 0));

        JLabel title = new JLabel("Settings");
        title.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(java.awt.Font.BOLD, 29f));
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Manage your preferences");
        subtitle.setFont(ThemeManager.FONT_BODY.deriveFont(13f));
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Scrollable content
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        buildSections();
        add(ThemeManager.createStyledScrollPane(content), BorderLayout.CENTER);
    }

    public void refresh() {
        if (content != null) {
            content.removeAll();
            buildSections();
            content.revalidate();
            content.repaint();
        }
    }

    private void buildSections() {
        content.add(buildSection("Account & Authentication",
                buildAccountSection()));
        content.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));
        content.add(buildSection("Appearance",
                buildAppearanceSection()));
        content.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));
        content.add(buildSection("Data & Privacy",
                buildDataSection()));
    }

    private JPanel buildAccountSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        com.aftercredits.client.auth.SessionManager.Session session =
                com.aftercredits.client.auth.SessionManager.getSession();

        String status;
        if (session != null && session.isGuest()) {
            status = "Guest (ID: " + (session.getGuestId() != null ? session.getGuestId() : "Active") + ")";
        } else if (session != null) {
            status = session.getDisplayName() + (session.getEmail() != null ? " (" + session.getEmail() + ")" : "");
        } else {
            status = "Local Demo User";
        }

        panel.add(buildSettingRow(
                "Current Session",
                "Your active AfterCredits identity",
                buildLabeledValue(status)));
        panel.add(ThemeManager.createDivider());
        panel.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));

        JButton authBtn = new JButton(session != null && session.isGuest() ? "Sign In / Register" : "Switch Account");
        authBtn.setFont(ThemeManager.FONT_BUTTON);
        authBtn.setBackground(ThemeManager.ACCENT_GOLD);
        authBtn.setForeground(java.awt.Color.BLACK);
        authBtn.setFocusPainted(false);
        authBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        authBtn.addActionListener(e -> {
            com.aftercredits.ui.LoginFrame.showAsDialog(this, s -> {
                refresh();
                if (onSessionUpdated != null) {
                    onSessionUpdated.run();
                }
            });
        });

        panel.add(buildSettingRow(
                "Account Access",
                "Sign in with your email/username or social login",
                authBtn));

        return panel;
    }

    /** Wraps a section body in a titled RoundedPanel card. */
    private JPanel buildSection(String sectionTitle, JPanel body) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10_000));

        JLabel label = new JLabel(sectionTitle);
        label.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(java.awt.Font.BOLD, 16f));
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeManager.SPACING_MD, 0));
        wrapper.add(label, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(ThemeManager.SPACING_XL, ThemeManager.SPACING_XL,
                        ThemeManager.SPACING_XL, ThemeManager.SPACING_XL)
        ));
        card.add(body, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildAppearanceSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(buildSettingRow(
                "Theme",
                "Application colour scheme and visual style.",
                buildThemeSelector()));
        panel.add(ThemeManager.createDivider());
        panel.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));
        panel.add(buildSettingRow(
                "Card Size",
                "Size of movie cards displayed in grids and rows.",
                buildLabeledValue("Standard (200 × 300)")));

        return panel;
    }

    private JPanel buildDataSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(buildSettingRow(
                "Cache",
                "Locally cached poster images and search results.",
                buildLabeledValue("Not managed in offline mode")));

        panel.add(ThemeManager.createDivider());
        panel.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));

        JButton exportButton = ThemeManager.createGhostButton("Export CSV");
        exportButton.addActionListener(e -> exportLibraryCsv());

        panel.add(buildSettingRow(
                "Library Export",
                "Export your watched list, personal ratings and dates.",
                exportButton));

        return panel;
    }

    private void exportLibraryCsv() {
        List<Movie> movies = repository.getWatchedMovies();

        if (movies == null || movies.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Your watched library is empty.",
                    "Nothing to Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Your AfterCredits Library");
        chooser.setSelectedFile(new java.io.File("aftercredits-library.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path output = chooser.getSelectedFile().toPath();
        String name = output.getFileName().toString();
        if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            output = output.resolveSibling(name + ".csv");
        }

        Map<String, Double> ratings = repository.getPersonalRatings();

        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write("Movie ID,Title,Year,Watched Date,Personal Rating");
            writer.newLine();

            for (Movie movie : movies) {
                String rating = ratings.containsKey(movie.id())
                        ? String.valueOf(ratings.get(movie.id())) : "";

                writer.write(csvValue(movie.id()) + ","
                        + csvValue(movie.title()) + ","
                        + csvValue(String.valueOf(movie.year())) + ","
                        + csvValue(repository.getWatchedDate(movie.id())) + ","
                        + csvValue(rating));
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this,
                    "Library exported successfully:\n" + output.toAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csvValue(String value) {
        return "\"" + (value == null ? "" : value.replace("\"", "\"\"")) + "\"";
    }

    /** Two-column row: left=label+hint, right=control. */
    private JPanel buildSettingRow(String label, String hint, java.awt.Component control) {
        JPanel row = new JPanel(new BorderLayout(ThemeManager.SPACING_LG, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, ThemeManager.SPACING_LG, 0));

        JPanel labelBlock = new JPanel();
        labelBlock.setLayout(new BoxLayout(labelBlock, BoxLayout.Y_AXIS));
        labelBlock.setOpaque(false);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(ThemeManager.FONT_BUTTON.deriveFont(java.awt.Font.BOLD, 13f));
        nameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(ThemeManager.FONT_SECONDARY.deriveFont(12f));
        hintLabel.setForeground(ThemeManager.TEXT_MUTED);

        labelBlock.add(nameLabel);
        labelBlock.add(Box.createVerticalStrut(ThemeManager.SPACING_XS));
        labelBlock.add(hintLabel);

        row.add(labelBlock, BorderLayout.CENTER);
        row.add(control, BorderLayout.EAST);
        return row;
    }

    private JPanel buildThemeSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panel.setOpaque(false);

        for (String theme : new String[]{"Dark", "Dim", "Light"}) {
            JButton btn = new JButton(theme);
            btn.setFont(ThemeManager.FONT_SECONDARY);
            btn.setPreferredSize(new Dimension(68, 34));
            btn.setFocusPainted(false);
            btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            boolean selected = theme.equals(ThemeManager.getTheme());
            if (selected) {
                btn.setBackground(ThemeManager.ACCENT_GOLD_BG);
                btn.setForeground(ThemeManager.ACCENT_GOLD);
                btn.setBorder(BorderFactory.createLineBorder(
                        ThemeManager.ACCENT_GOLD_MUTED, 1, true));
            } else {
                btn.setBackground(ThemeManager.BG_INPUT);
                btn.setForeground(ThemeManager.TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createLineBorder(
                        ThemeManager.BORDER_COLOR, 1, true));
            }

            btn.addActionListener(e -> onThemeChange.accept(theme));
            panel.add(btn);
        }
        return panel;
    }

    private JLabel buildLabeledValue(String value) {
        JLabel lbl = new JLabel(value);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        return lbl;
    }
}
