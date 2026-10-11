package com.aftercredits.ui.views;

import com.aftercredits.model.UserProfile;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.DnaBarChart;
import com.aftercredits.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * Modern profile view displaying user stats, avatar, and Film DNA.
 */
public class ProfileView extends JPanel {

    public ProfileView(CinemaRepository repository) {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        
        UserProfile profile = repository.getUserProfile();
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(ThemeManager.createViewPadding());

        // Header profile card
        RoundedPanel headerCard = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        headerCard.setLayout(new BorderLayout(20, 20));
        headerCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        headerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        headerCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel avatar = new JLabel("👤", JLabel.CENTER);
        avatar.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(48f));
        avatar.setPreferredSize(new Dimension(100, 100));
        avatar.setOpaque(true);
        avatar.setBackground(ThemeManager.BG_INPUT);
        avatar.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR));

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);
        
        JLabel nameLabel = new JLabel(profile.username() + " (" + profile.handle() + ")");
        nameLabel.setFont(ThemeManager.FONT_PAGE_TITLE);
        nameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel identityLabel = new JLabel(profile.cinemaIdentity());
        identityLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        identityLabel.setForeground(ThemeManager.ACCENT_GOLD);

        JLabel bioLabel = new JLabel(profile.bio());
        bioLabel.setFont(ThemeManager.FONT_BODY);
        bioLabel.setForeground(ThemeManager.TEXT_MUTED);

        userInfo.add(nameLabel);
        userInfo.add(Box.createVerticalStrut(4));
        userInfo.add(identityLabel);
        userInfo.add(Box.createVerticalStrut(10));
        userInfo.add(bioLabel);

        headerCard.add(avatar, BorderLayout.WEST);
        headerCard.add(userInfo, BorderLayout.CENTER);
        content.add(headerCard);
        content.add(Box.createVerticalStrut(30));

        // Film DNA and Stats
        JPanel lowerGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        lowerGrid.setOpaque(false);
        lowerGrid.setAlignmentX(LEFT_ALIGNMENT);
        RoundedPanel statsPanel = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsPanel.add(ThemeManager.createSectionHeader("Cinema Stats", null));
        statsPanel.add(Box.createVerticalStrut(20));
        statsPanel.add(createStatRow("Total Films Logged", String.valueOf(profile.totalFilmsWatched())));
        statsPanel.add(createStatRow("Films This Year", String.valueOf(profile.filmsThisYear())));
        statsPanel.add(createStatRow("Reviews Written", String.valueOf(profile.reviewsWritten())));
        statsPanel.add(createStatRow("Lists Created", String.valueOf(profile.listsCount())));

        // DNA
        RoundedPanel dnaPanel = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        dnaPanel.setLayout(new BorderLayout());
        dnaPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dnaPanel.add(ThemeManager.createSectionHeader("Film DNA", "Top Genres"), BorderLayout.NORTH);
        dnaPanel.add(new DnaBarChart(profile.genrePercentages()), BorderLayout.CENTER);

        lowerGrid.add(statsPanel);
        lowerGrid.add(dnaPanel);
        
        content.add(lowerGrid);

        add(ThemeManager.createStyledScrollPane(content), BorderLayout.CENTER);
    }

    private JPanel createStatRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        
        JLabel val = new JLabel(value);
        val.setFont(ThemeManager.FONT_SECTION_HEADING);
        val.setForeground(ThemeManager.TEXT_PRIMARY);
        
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }
}
