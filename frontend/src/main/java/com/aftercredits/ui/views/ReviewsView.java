package com.aftercredits.ui.views;

import com.aftercredits.model.Review;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.RoundedPanel;
import com.aftercredits.ui.components.BadgeLabel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class ReviewsView extends JPanel {
    public ReviewsView(CinemaRepository repository) {
        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Community Reviews");
        title.setFont(ThemeManager.FONT_PAGE_TITLE);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        for (Review r : repository.getCommunityReviews()) {
            RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            card.setMaximumSize(new Dimension(800, 200));
            card.setAlignmentX(LEFT_ALIGNMENT);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            topRow.setOpaque(false);
            topRow.setAlignmentX(LEFT_ALIGNMENT);
            
            JLabel movieTitle = new JLabel(r.movieTitle());
            movieTitle.setFont(ThemeManager.FONT_SECTION_HEADING);
            movieTitle.setForeground(ThemeManager.ACCENT_GOLD);
            topRow.add(movieTitle);
            topRow.add(BadgeLabel.createRatingBadge(r.rating()));

            if (r.containsSpoilers()) {
                topRow.add(BadgeLabel.createSpoilerBadge());
            }

            JLabel author = new JLabel("By " + r.author() + " • " + r.dateLogged());
            author.setFont(ThemeManager.FONT_SECONDARY);
            author.setForeground(ThemeManager.TEXT_MUTED);
            author.setAlignmentX(LEFT_ALIGNMENT);

            JLabel content = new JLabel("<html><body style='width: 700px;'>" + r.comment() + "</body></html>");
            content.setFont(ThemeManager.FONT_BODY);
            content.setForeground(ThemeManager.TEXT_PRIMARY);
            content.setAlignmentX(LEFT_ALIGNMENT);

            card.add(topRow);
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
