package com.aftercredits.ui.views;

import com.aftercredits.model.MovieGroup;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class GroupsView extends JPanel {
    public GroupsView(CinemaRepository repository) {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Movie Groups");
        title.setFont(ThemeManager.FONT_PAGE_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (MovieGroup g : repository.getAllMovieGroups()) {
            RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
            card.setLayout(new BorderLayout(20, 10));
            card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            card.setMaximumSize(new Dimension(800, 140));
            card.setAlignmentX(LEFT_ALIGNMENT);

            // Center details
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            titleRow.setOpaque(false);
            titleRow.setAlignmentX(LEFT_ALIGNMENT);
            
            JLabel groupName = new JLabel(g.name());
            groupName.setFont(ThemeManager.FONT_SECTION_HEADING);
            groupName.setForeground(ThemeManager.TEXT_PRIMARY);
            
            titleRow.add(groupName);
            titleRow.add(BadgeLabel.createGenreBadge(g.focusGenre()));
            titleRow.add(new BadgeLabel(g.memberCount() + " Members"));

            JLabel descLabel = new JLabel(g.description());
            descLabel.setFont(ThemeManager.FONT_BODY);
            descLabel.setForeground(ThemeManager.TEXT_MUTED);
            descLabel.setAlignmentX(LEFT_ALIGNMENT);

            JLabel currentDiscussion = new JLabel("Currently Discussing: " + g.currentDiscussionMovie());
            currentDiscussion.setFont(ThemeManager.FONT_SECONDARY);
            currentDiscussion.setForeground(ThemeManager.ACCENT_GOLD);
            currentDiscussion.setAlignmentX(LEFT_ALIGNMENT);

            infoPanel.add(titleRow);
            infoPanel.add(Box.createVerticalStrut(8));
            infoPanel.add(descLabel);
            infoPanel.add(Box.createVerticalStrut(8));
            infoPanel.add(currentDiscussion);

            // Right action
            JPanel actionPanel = new JPanel(new BorderLayout());
            actionPanel.setOpaque(false);
            JButton joinBtn = g.isJoined() ? ThemeManager.createGhostButton("Joined") : ThemeManager.createAccentButton("Join Group");
            actionPanel.add(joinBtn, BorderLayout.CENTER);

            card.add(infoPanel, BorderLayout.CENTER);
            card.add(actionPanel, BorderLayout.EAST);

            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(16));
        }

        add(ThemeManager.createStyledScrollPane(listPanel), BorderLayout.CENTER);
    }
}
