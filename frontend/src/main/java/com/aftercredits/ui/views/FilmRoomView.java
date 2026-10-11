package com.aftercredits.ui.views;

import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class FilmRoomView extends JPanel {
    public FilmRoomView(CinemaRepository repository) {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("The Film Room");
        title.setFont(ThemeManager.FONT_PAGE_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        
        JLabel subtitle = new JLabel("Deep dive discussions and film theory");
        subtitle.setFont(ThemeManager.FONT_BODY);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        header.add(subtitle, BorderLayout.SOUTH);
        
        add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (FilmRoomTopic t : repository.getAllFilmRoomTopics()) {
            RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            card.setMaximumSize(new Dimension(800, 220));
            card.setAlignmentX(LEFT_ALIGNMENT);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            topRow.setOpaque(false);
            topRow.setAlignmentX(LEFT_ALIGNMENT);
            
            topRow.add(BadgeLabel.createGenreBadge(t.category()));
            topRow.add(new BadgeLabel(t.movieTitle()));
            if (t.hasSpoilers()) {
                topRow.add(BadgeLabel.createSpoilerBadge());
            }

            JLabel topicTitle = new JLabel(t.topicTitle());
            topicTitle.setFont(ThemeManager.FONT_SECTION_HEADING);
            topicTitle.setForeground(ThemeManager.TEXT_PRIMARY);
            topicTitle.setAlignmentX(LEFT_ALIGNMENT);

            JLabel author = new JLabel("Started by " + t.author() + " • " + t.upvotes() + " upvotes • " + t.commentCount() + " comments");
            author.setFont(ThemeManager.FONT_SECONDARY);
            author.setForeground(ThemeManager.ACCENT_GOLD_MUTED);
            author.setAlignmentX(LEFT_ALIGNMENT);

            JLabel content = new JLabel("<html><body style='width: 700px; color: #8C96A5;'>" + t.content() + "</body></html>");
            content.setFont(ThemeManager.FONT_BODY);
            content.setAlignmentX(LEFT_ALIGNMENT);

            card.add(topRow);
            card.add(Box.createVerticalStrut(10));
            card.add(topicTitle);
            card.add(Box.createVerticalStrut(4));
            card.add(author);
            card.add(Box.createVerticalStrut(12));
            card.add(content);

            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(16));
        }

        add(ThemeManager.createStyledScrollPane(listPanel), BorderLayout.CENTER);
    }
}
