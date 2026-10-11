package com.aftercredits.ui.views;

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

    public SettingsView(CinemaRepository repository) {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        // Page header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeManager.SPACING_XL, 0));

        JLabel title = new JLabel("Settings");
        title.setFont(ThemeManager.FONT_PAGE_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Manage your preferences and API configuration");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Scrollable content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(buildSection("API Configuration",
                buildApiSection(repository)));
        content.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));
        content.add(buildSection("Appearance",
                buildAppearanceSection()));
        content.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));
        content.add(buildSection("Data & Privacy",
                buildDataSection()));

        add(ThemeManager.createStyledScrollPane(content), BorderLayout.CENTER);
    }

    /** Wraps a section body in a titled RoundedPanel card. */
    private JPanel buildSection(String sectionTitle, JPanel body) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10_000));

        JLabel label = new JLabel(sectionTitle);
        label.setFont(ThemeManager.FONT_SECTION_HEADING);
        label.setForeground(ThemeManager.TEXT_PRIMARY);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeManager.SPACING_SM, 0));
        wrapper.add(label, BorderLayout.NORTH);

        RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_LG, ThemeManager.SPACING_LG,
                ThemeManager.SPACING_LG, ThemeManager.SPACING_LG));
        card.add(body, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildApiSection(CinemaRepository repository) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(buildSettingRow(
                "TMDB API Key",
                "Used for live movie data, posters, and search results.",
                buildApiKeyField()));

        panel.add(ThemeManager.createDivider());
        panel.add(Box.createVerticalStrut(ThemeManager.SPACING_MD));

        // Connection status indicator
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.SPACING_SM, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel statusIcon = new JLabel("●");
        statusIcon.setFont(ThemeManager.FONT_BODY);
        // Show green if TMDB is live, amber if offline/mock
        boolean isLive = repository instanceof com.aftercredits.repository.TmdbCinemaRepository
                && ((com.aftercredits.repository.TmdbCinemaRepository) repository).isLiveMode();
        statusIcon.setForeground(isLive ? ThemeManager.SUCCESS : ThemeManager.WARNING);

        JLabel statusLabel = new JLabel(isLive
                ? "Connected to TMDB (live data)"
                : "Offline mode — using pre-seeded fallback data");
        statusLabel.setFont(ThemeManager.FONT_BODY);
        statusLabel.setForeground(ThemeManager.TEXT_MUTED);

        statusRow.add(statusIcon);
        statusRow.add(statusLabel);
        panel.add(statusRow);

        return panel;
    }

    private JPanel buildApiKeyField() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.SPACING_SM, 0));
        row.setOpaque(false);

        // Masked API key field
        RoundedPanel fieldContainer = new RoundedPanel(ThemeManager.BG_INPUT, 8, ThemeManager.BORDER_COLOR);
        fieldContainer.setLayout(new BorderLayout(8, 0));
        fieldContainer.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        fieldContainer.setPreferredSize(new Dimension(320, 36));

        JTextField keyField = new JTextField("••••••••••••••••••••••••••••••••");
        keyField.setOpaque(false);
        keyField.setBorder(null);
        keyField.setForeground(ThemeManager.TEXT_MUTED);
        keyField.setFont(ThemeManager.FONT_BODY);
        keyField.setEditable(false);

        fieldContainer.add(keyField, BorderLayout.CENTER);
        row.add(fieldContainer);

        JButton editBtn = ThemeManager.createGhostButton("Edit");
        editBtn.setPreferredSize(new Dimension(60, 36));
        row.add(editBtn);

        return row;
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
        panel.add(buildSettingRow(
                "Library Export",
                "Export your watched list and ratings.",
                ThemeManager.createGhostButton("Export CSV")));

        return panel;
    }

    /** Two-column row: left=label+hint, right=control. */
    private JPanel buildSettingRow(String label, String hint, java.awt.Component control) {
        JPanel row = new JPanel(new BorderLayout(ThemeManager.SPACING_LG, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeManager.SPACING_MD, 0));

        JPanel labelBlock = new JPanel();
        labelBlock.setLayout(new BoxLayout(labelBlock, BoxLayout.Y_AXIS));
        labelBlock.setOpaque(false);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(ThemeManager.FONT_BUTTON);
        nameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(ThemeManager.FONT_SECONDARY);
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

        // Only "Dark" is active; other options shown as disabled for demonstration
        for (String theme : new String[]{"Dark", "Dim", "Light"}) {
            JButton btn = new JButton(theme);
            btn.setFont(ThemeManager.FONT_SECONDARY);
            btn.setPreferredSize(new Dimension(60, 30));
            btn.setFocusPainted(false);
            if ("Dark".equals(theme)) {
                btn.setBackground(ThemeManager.ACCENT_GOLD_BG);
                btn.setForeground(ThemeManager.ACCENT_GOLD);
                btn.setBorder(BorderFactory.createLineBorder(ThemeManager.ACCENT_GOLD_MUTED, 1, true));
            } else {
                btn.setBackground(ThemeManager.BG_INPUT);
                btn.setForeground(ThemeManager.TEXT_FAINT);
                btn.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1, true));
            }
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
