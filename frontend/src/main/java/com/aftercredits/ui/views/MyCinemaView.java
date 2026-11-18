package com.aftercredits.ui.views;

import com.aftercredits.model.ActivityEvent;

import com.aftercredits.model.CustomWatchlist;
import com.aftercredits.model.Movie;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.repository.WatchlistManager;
import com.aftercredits.theme.ThemeManager;
import com.aftercredits.ui.components.BadgeLabel;
import com.aftercredits.ui.components.MovieCard;
import com.aftercredits.ui.components.RoundedPanel;
import com.aftercredits.ui.util.ScrollWheelForwarder;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Modernized view for managing the user's personal cinema library.
 * Tabs: Watched, Watchlist (Default & Custom Watchlists with Private/Public options), Diary, Collections.
 */
public class MyCinemaView extends JPanel {

    private final CinemaRepository repository;
    private final Consumer<Movie> onMovieSelected;
    private final JTabbedPane tabbedPane;

    private final JPanel watchedContainer = new JPanel(new BorderLayout());
    private final JPanel watchlistContainer = new JPanel(new BorderLayout());
    private final JPanel diaryContainer = new JPanel(new BorderLayout());
    private final JPanel collectionsContainer = new JPanel(new BorderLayout());
    private final JPanel favouritesContainer = new JPanel(new BorderLayout());
    private final JPanel activityContainer = new JPanel(new BorderLayout());

    private String selectedWatchlistId = "DEFAULT";

    public MyCinemaView(CinemaRepository repository, Consumer<Movie> onMovieSelected) {
        this.repository = repository;
        this.onMovieSelected = onMovieSelected;

        setLayout(new BorderLayout());
        setBackground(ThemeManager.BG_PRIMARY);
        setBorder(ThemeManager.createViewPadding());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeManager.SPACING_XL, 0));

        JLabel title = new JLabel("My Cinema");
        title.setFont(ThemeManager.FONT_PAGE_TITLE.deriveFont(java.awt.Font.BOLD, 29f));
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(java.awt.Font.BOLD, 13f));
        tabbedPane.setForeground(ThemeManager.TEXT_MUTED);
        tabbedPane.setBackground(ThemeManager.BG_PRIMARY);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        watchedContainer.setOpaque(false);
        watchlistContainer.setOpaque(false);
        diaryContainer.setOpaque(false);
        collectionsContainer.setOpaque(false);
        activityContainer.setOpaque(false);
        favouritesContainer.setOpaque(false);

        tabbedPane.addTab("Watched", watchedContainer);
        tabbedPane.addTab("Watchlist", watchlistContainer);
        tabbedPane.addTab("Diary", diaryContainer);
        tabbedPane.addTab("Collections", collectionsContainer);
        tabbedPane.addTab("Favourites", favouritesContainer);
        tabbedPane.addTab("Activity", activityContainer);

        tabbedPane.setTabComponentAt(0, createCustomTabComponent("Watched", new TabIcon(TabIcon.Type.WATCHED)));
        tabbedPane.setTabComponentAt(1, createCustomTabComponent("Watchlist", new TabIcon(TabIcon.Type.WATCHLIST)));
        tabbedPane.setTabComponentAt(2, createCustomTabComponent("Diary", new TabIcon(TabIcon.Type.DIARY)));
        tabbedPane.setTabComponentAt(3, createCustomTabComponent("Collections", new TabIcon(TabIcon.Type.COLLECTIONS)));
        tabbedPane.setTabComponentAt(4, createCustomTabComponent("Favourites", new TabIcon(TabIcon.Type.WATCHED)));
        tabbedPane.setTabComponentAt(5, createCustomTabComponent("Activity", new TabIcon(TabIcon.Type.DIARY)));

        add(tabbedPane, BorderLayout.CENTER);

        // Auto-refresh when watchlists are updated anywhere in the app
        WatchlistManager.getInstance().addChangeListener(() -> {
            SwingUtilities.invokeLater(this::refresh);
        });

        refresh();
    }

    private JPanel createCustomTabComponent(String title, javax.swing.Icon icon) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(title);
        textLabel.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(java.awt.Font.BOLD, 13f));
        textLabel.setForeground(ThemeManager.TEXT_MUTED);

        panel.add(iconLabel);
        panel.add(textLabel);

        tabbedPane.addChangeListener(e -> {
            boolean isSelected = tabbedPane.indexOfTabComponent(panel) == tabbedPane.getSelectedIndex();
            textLabel.setForeground(isSelected ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            panel.setForeground(isSelected ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            iconLabel.setForeground(isSelected ? ThemeManager.TEXT_PRIMARY : ThemeManager.TEXT_MUTED);
            panel.repaint();
        });

        panel.setForeground(ThemeManager.TEXT_MUTED);
        iconLabel.setForeground(ThemeManager.TEXT_MUTED);

        return panel;
    }

    /** Switches the visible tab based on the sidebar navigation key. */
    public void selectTab(String navKey) {
        switch (navKey) {
            case "WATCHED": tabbedPane.setSelectedIndex(0); break;
            case "WATCHLIST": tabbedPane.setSelectedIndex(1); break;
            case "DIARY": tabbedPane.setSelectedIndex(2); break;
            case "COLLECTIONS": tabbedPane.setSelectedIndex(3); break;
            case "FAVOURITES": tabbedPane.setSelectedIndex(4); break;
            case "ACTIVITY": tabbedPane.setSelectedIndex(5); break;
            case "MY_CINEMA": tabbedPane.setSelectedIndex(0); break;
        }
        refresh();
    }

    /** Refreshes all tabs with latest state. */
    public void refresh() {
        refreshWatchedTab();
        refreshWatchlistTab();
        refreshDiaryTab();
        refreshCollectionsTab();
        refreshFavouritesTab();
        refreshActivityTab();
        revalidate();
        repaint();
    }

    // ─── Watched Tab ─────────────────────────────────────────────────────────

    private void refreshWatchedTab() {
        watchedContainer.removeAll();
        watchedContainer.add(createMovieGrid(repository.getWatchedMovies(), onMovieSelected), BorderLayout.CENTER);
        watchedContainer.revalidate();
        watchedContainer.repaint();
    }

    private JPanel createMovieGrid(List<Movie> movies, Consumer<Movie> onMovieSelected) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_XL, 0, 0, 0));

        if (movies.isEmpty()) {
            panel.add(ThemeManager.createEmptyState("🎞️", "Your library is empty",
                    "Start exploring and log movies as watched to build your cinema journey."), BorderLayout.CENTER);
            return panel;
        }

        JPanel wrapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, ThemeManager.CARD_GAP));
        wrapPanel.setOpaque(false);
        for (Movie m : movies) {
            wrapPanel.add(new MovieCard(m, onMovieSelected));
        }
        panel.add(ThemeManager.createStyledScrollPane(wrapPanel), BorderLayout.CENTER);
        return panel;
    }

    // ─── Watchlist Tab (Default & Custom Watchlists) ──────────────────────────

    private void refreshWatchlistTab() {
        watchlistContainer.removeAll();

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_MD, 0, 0, 0));

        // 1. Top Control Bar: Title & + Create Watchlist Button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel subtitle = new JLabel("Curate and organize your cinema queues with public or private visibility");
        subtitle.setFont(ThemeManager.FONT_SECONDARY);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);

        titleBox.add(subtitle);
        topBar.add(titleBox, BorderLayout.WEST);

        JButton createBtn = ThemeManager.createAccentButton("+  Create Watchlist");
        createBtn.addActionListener(e -> showCreateWatchlistDialog());
        topBar.add(createBtn, BorderLayout.EAST);

        // 2. Watchlist Selector Pills
        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        selectorBar.setOpaque(false);

        List<CustomWatchlist> customWatchlists = repository.getCustomWatchlists();
        int defaultCount = repository.getWatchlist().size();

        // Default Watchlist Pill
        boolean isDefaultSelected = "DEFAULT".equals(selectedWatchlistId);
        JButton defaultPill = createWatchlistPill("★  Default Watchlist (" + defaultCount + ")", isDefaultSelected);
        defaultPill.addActionListener(e -> {
            selectedWatchlistId = "DEFAULT";
            refreshWatchlistTab();
        });
        selectorBar.add(defaultPill);

        // Custom Watchlist Pills
        for (CustomWatchlist wl : customWatchlists) {
            boolean isSelected = wl.getId().equals(selectedWatchlistId);
            String label = wl.getName() + " (" + wl.getPrivacyLabel() + " • " + wl.getMovieCount() + ")";
            JButton pill = createWatchlistPill(label, isSelected);
            pill.addActionListener(e -> {
                selectedWatchlistId = wl.getId();
                refreshWatchlistTab();
            });
            selectorBar.add(pill);
        }

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(topBar);
        topSection.add(selectorBar);
        topSection.add(Box.createVerticalStrut(12));
        topSection.add(ThemeManager.createDivider());
        topSection.add(Box.createVerticalStrut(14));

        root.add(topSection, BorderLayout.NORTH);

        // 3. Active Watchlist Content
        if ("DEFAULT".equals(selectedWatchlistId)) {
            root.add(buildDefaultWatchlistContent(), BorderLayout.CENTER);
        } else {
            Optional<CustomWatchlist> opt = repository.getCustomWatchlist(selectedWatchlistId);
            if (opt.isPresent()) {
                root.add(buildCustomWatchlistContent(opt.get()), BorderLayout.CENTER);
            } else {
                selectedWatchlistId = "DEFAULT";
                root.add(buildDefaultWatchlistContent(), BorderLayout.CENTER);
            }
        }

        watchlistContainer.add(root, BorderLayout.CENTER);
        watchlistContainer.revalidate();
        watchlistContainer.repaint();
    }

    private JButton createWatchlistPill(String text, boolean isSelected) {
        JButton pill = ThemeManager.createGhostButton(text);
        if (isSelected) {
            pill.setBackground(ThemeManager.BG_CARD);
            pill.setForeground(ThemeManager.ACCENT_GOLD);
            pill.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.ACCENT_GOLD, 1),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)
            ));
        } else {
            pill.setBackground(ThemeManager.BG_PRIMARY);
            pill.setForeground(ThemeManager.TEXT_MUTED);
            pill.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(6, 14, 6, 14)
            ));
        }
        return pill;
    }

    private JPanel buildDefaultWatchlistContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        List<Movie> movies = repository.getWatchlist();

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel info = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel("Default Watchlist");
        nameLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        nameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        info.add(nameLabel);
        info.add(new BadgeLabel("★ PRIMARY LIST"));
        info.add(new BadgeLabel(movies.size() + " films"));

        headerPanel.add(info, BorderLayout.WEST);
        panel.add(headerPanel, BorderLayout.NORTH);

        if (movies.isEmpty()) {
            panel.add(ThemeManager.createEmptyState("📋", "Your Watchlist is empty",
                    "Browse films on Home or Discover and click '+ Watchlist' to save movies here."), BorderLayout.CENTER);
            return panel;
        }

        JPanel wrapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, ThemeManager.CARD_GAP));
        wrapPanel.setOpaque(false);
        for (Movie m : movies) {
            wrapPanel.add(new MovieCard(m, onMovieSelected));
        }

        panel.add(ThemeManager.createStyledScrollPane(wrapPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCustomWatchlistContent(CustomWatchlist watchlist) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        List<Movie> movies = repository.getMoviesForCustomWatchlist(watchlist.getId());

        // Header with details & actions
        RoundedPanel headerCard = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        headerCard.setLayout(new BorderLayout(14, 0));
        headerCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(watchlist.getName());
        nameLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        nameLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        titleRow.add(nameLabel);
        titleRow.add(new BadgeLabel(watchlist.getPrivacyLabel()));
        titleRow.add(new BadgeLabel(movies.size() + " films"));

        leftCol.add(titleRow);

        if (watchlist.getDescription() != null && !watchlist.getDescription().isBlank()) {
            leftCol.add(Box.createVerticalStrut(4));
            JLabel descLabel = new JLabel(watchlist.getDescription());
            descLabel.setFont(ThemeManager.FONT_SECONDARY);
            descLabel.setForeground(ThemeManager.TEXT_MUTED);
            descLabel.setAlignmentX(LEFT_ALIGNMENT);
            leftCol.add(descLabel);
        }

        headerCard.add(leftCol, BorderLayout.CENTER);

        // Action Buttons: Add Films, Toggle Privacy & Delete
        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsRow.setOpaque(false);

        JButton addFilmsBtn = ThemeManager.createGhostButton("+  Add Films");
        addFilmsBtn.addActionListener(e -> showAddFilmsToWatchlistDialog(watchlist));

        String privacyActionText = watchlist.isPrivate() ? "🌐  Make Public" : "🔒  Make Private";
        JButton togglePrivacyBtn = ThemeManager.createGhostButton(privacyActionText);
        togglePrivacyBtn.addActionListener(e -> {
            boolean newPrivacy = !watchlist.isPrivate();
            repository.setCustomWatchlistPrivacy(watchlist.getId(), newPrivacy);
            refreshWatchlistTab();
        });

        JButton deleteBtn = ThemeManager.createGhostButton("🗑️  Delete List");
        deleteBtn.setForeground(new java.awt.Color(239, 83, 80));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete the watchlist \"" + watchlist.getName() + "\"?",
                    "Delete Watchlist",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                repository.deleteCustomWatchlist(watchlist.getId());
                selectedWatchlistId = "DEFAULT";
                refreshWatchlistTab();
            }
        });

        actionsRow.add(addFilmsBtn);
        actionsRow.add(togglePrivacyBtn);
        actionsRow.add(deleteBtn);
        headerCard.add(actionsRow, BorderLayout.EAST);

        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setOpaque(false);
        contentContainer.add(headerCard, BorderLayout.NORTH);

        if (movies.isEmpty()) {
            JPanel empty = ThemeManager.createEmptyState("🎬", "No movies in this list yet",
                    "Open any movie details and click '+ Watchlist' to add it to \"" + watchlist.getName() + "\".");
            contentContainer.add(empty, BorderLayout.CENTER);
        } else {
            JPanel wrapPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, ThemeManager.CARD_GAP));
            wrapPanel.setOpaque(false);
            wrapPanel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

            for (Movie m : movies) {
                JPanel cardContainer = new JPanel(new BorderLayout(0, 4));
                cardContainer.setOpaque(false);
                cardContainer.add(new MovieCard(m, onMovieSelected), BorderLayout.CENTER);

                JButton removeBtn = ThemeManager.createGhostButton("✕ Remove from List");
                removeBtn.setFont(ThemeManager.FONT_METADATA.deriveFont(10f));
                removeBtn.setForeground(ThemeManager.TEXT_MUTED);
                removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                removeBtn.addActionListener(e -> {
                    repository.removeMovieFromCustomWatchlist(watchlist.getId(), m.id());
                    refreshWatchlistTab();
                });
                cardContainer.add(removeBtn, BorderLayout.SOUTH);

                wrapPanel.add(cardContainer);
            }
            contentContainer.add(ThemeManager.createStyledScrollPane(wrapPanel), BorderLayout.CENTER);
        }

        panel.add(contentContainer, BorderLayout.CENTER);
        return panel;
    }

    private void showCreateWatchlistDialog() {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Create Custom Watchlist",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel("New Custom Watchlist");
        titleLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(14));

        JLabel nameLabel = new JLabel("Watchlist Name *");
        nameLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        nameLabel.setForeground(ThemeManager.ACCENT_GOLD);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(4));

        JTextField nameField = ThemeManager.createStyledTextField();
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(nameField);
        panel.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel("Description (Optional)");
        descLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        descLabel.setForeground(ThemeManager.TEXT_MUTED);
        descLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(4));

        JTextField descField = ThemeManager.createStyledTextField();
        descField.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(descField);
        panel.add(Box.createVerticalStrut(14));

        JLabel privacyLabel = new JLabel("Privacy Option *");
        privacyLabel.setFont(ThemeManager.FONT_METADATA.deriveFont(Font.BOLD, 10f));
        privacyLabel.setForeground(ThemeManager.ACCENT_GOLD);
        privacyLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(privacyLabel);
        panel.add(Box.createVerticalStrut(6));

        JRadioButton privateRadio = new JRadioButton("🔒 Private — Only visible to you in your personal library", true);
        privateRadio.setFont(ThemeManager.FONT_BODY);
        privateRadio.setForeground(ThemeManager.TEXT_PRIMARY);
        privateRadio.setOpaque(false);
        privateRadio.setAlignmentX(LEFT_ALIGNMENT);

        JRadioButton publicRadio = new JRadioButton("🌐 Public — Visible on your profile & to the AfterCredits community", false);
        publicRadio.setFont(ThemeManager.FONT_BODY);
        publicRadio.setForeground(ThemeManager.TEXT_PRIMARY);
        publicRadio.setOpaque(false);
        publicRadio.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        group.add(privateRadio);
        group.add(publicRadio);

        panel.add(privateRadio);
        panel.add(Box.createVerticalStrut(6));
        panel.add(publicRadio);
        panel.add(Box.createVerticalStrut(20));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(LEFT_ALIGNMENT);

        JButton cancelBtn = ThemeManager.createGhostButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton createBtn = ThemeManager.createAccentButton("Create Watchlist");
        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a name for your watchlist.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean isPrivate = privateRadio.isSelected();
            CustomWatchlist newWl = repository.createCustomWatchlist(name, descField.getText().trim(), isPrivate);
            selectedWatchlistId = newWl.getId();
            dialog.dispose();
            refreshWatchlistTab();
        });

        buttons.add(cancelBtn);
        buttons.add(createBtn);
        panel.add(buttons);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showAddFilmsToWatchlistDialog(CustomWatchlist watchlist) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Add Films to " + watchlist.getName(),
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Header with search box
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("Add Films to \"" + watchlist.getName() + "\"");
        titleLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Search or toggle films to include in this custom list:");
        subLabel.setFont(ThemeManager.FONT_SECONDARY);
        subLabel.setForeground(ThemeManager.TEXT_MUTED);

        JTextField searchField = ThemeManager.createStyledTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search films by title or genre...");

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subLabel);
        header.add(Box.createVerticalStrut(10));
        header.add(searchField);
        header.add(Box.createVerticalStrut(12));
        header.add(ThemeManager.createDivider());

        panel.add(header, BorderLayout.NORTH);

        // Movie list container
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        List<Movie> allAvailable = new ArrayList<>();
        allAvailable.addAll(repository.getAllMovies());
        if (allAvailable.isEmpty()) {
            allAvailable.addAll(repository.getPopularMovies());
        }

        Runnable populateList = new Runnable() {
            @Override
            public void run() {
                listContainer.removeAll();
                String filter = searchField.getText().trim().toLowerCase();

                List<Movie> filtered = allAvailable.stream()
                        .filter(m -> filter.isEmpty() || m.title().toLowerCase().contains(filter)
                                || m.genres().stream().anyMatch(g -> g.toLowerCase().contains(filter)))
                        .collect(Collectors.toList());

                if (filtered.isEmpty()) {
                    JLabel emptyLabel = new JLabel("<html><i>No films matched your search.</i></html>");
                    emptyLabel.setFont(ThemeManager.FONT_SECONDARY);
                    emptyLabel.setForeground(ThemeManager.TEXT_MUTED);
                    listContainer.add(emptyLabel);
                } else {
                    for (Movie m : filtered) {
                        JPanel row = new JPanel(new BorderLayout(10, 0));
                        row.setOpaque(false);
                        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

                        boolean inWl = watchlist.containsMovie(m.id());
                        javax.swing.JCheckBox check = new javax.swing.JCheckBox("", inWl);
                        check.setOpaque(false);

                        JLabel movieTitle = new JLabel(m.title() + " (" + m.year() + ")");
                        movieTitle.setFont(ThemeManager.FONT_BODY);
                        movieTitle.setForeground(ThemeManager.TEXT_PRIMARY);

                        check.addActionListener(e -> {
                            if (check.isSelected()) {
                                repository.addMovieToCustomWatchlist(watchlist.getId(), m.id());
                            } else {
                                repository.removeMovieFromCustomWatchlist(watchlist.getId(), m.id());
                            }
                            refreshWatchlistTab();
                        });

                        row.add(check, BorderLayout.WEST);
                        row.add(movieTitle, BorderLayout.CENTER);
                        listContainer.add(row);
                    }
                }

                listContainer.revalidate();
                listContainer.repaint();
            }
        };

        populateList.run();

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { populateList.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { populateList.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { populateList.run(); }
        });

        JScrollPane scroll = ThemeManager.createStyledScrollPane(listContainer);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        // Footer Done button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton doneBtn = ThemeManager.createAccentButton("Done");
        doneBtn.addActionListener(e -> {
            dialog.dispose();
            refreshWatchlistTab();
        });
        footer.add(doneBtn);
        panel.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ─── Diary Tab ───────────────────────────────────────────────────────────

    private void refreshDiaryTab() {
        diaryContainer.removeAll();
        diaryContainer.add(createDiaryTab(repository), BorderLayout.CENTER);
        diaryContainer.revalidate();
        diaryContainer.repaint();
    }

    private JPanel createDiaryTab(CinemaRepository repository) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_LG, 0, 0, 0));

        List<Movie> watched = repository.getWatchedMovies();
        if (watched.isEmpty()) {
            panel.add(ThemeManager.createEmptyState("📔", "Your diary is empty",
                    "Movies you mark as watched will appear here as diary entries."), BorderLayout.CENTER);
            return panel;
        }

        Map<String, List<Movie>> byYear = watched.stream()
                .collect(Collectors.groupingBy(m -> {
                    String date = repository.getWatchedDate(m.id());
                    return date != null && date.length() >= 4
                            ? date.substring(0, 4) : "Earlier entries";
                }, Collectors.toList()));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        byYear.entrySet().stream()
                .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .forEach(entry -> {
                    JLabel periodLabel = new JLabel(entry.getKey());
                    periodLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
                    periodLabel.setForeground(ThemeManager.ACCENT_GOLD);
                    periodLabel.setAlignmentX(LEFT_ALIGNMENT);
                    periodLabel.setBorder(BorderFactory.createEmptyBorder(
                            ThemeManager.SPACING_MD, 0, ThemeManager.SPACING_SM, 0));
                    listPanel.add(periodLabel);
                    listPanel.add(ThemeManager.createDivider());
                    listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));

                    entry.getValue().stream()
                            .sorted((a, b) -> {
                                String dateA = repository.getWatchedDate(a.id());
                                String dateB = repository.getWatchedDate(b.id());
                                return (dateB == null ? "" : dateB)
                                        .compareTo(dateA == null ? "" : dateA);
                            })
                            .forEach(m -> {
                                listPanel.add(buildDiaryEntry(m, repository.getWatchedDate(m.id())));
                                listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));
                            });
                });

        panel.add(ThemeManager.createStyledScrollPane(listPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDiaryEntry(Movie movie, String watchedDate) {
        RoundedPanel card = new RoundedPanel(ThemeManager.BG_CARD, ThemeManager.CARD_ARC, ThemeManager.BORDER_COLOR);
        card.setLayout(new BorderLayout(ThemeManager.SPACING_MD, 0));
        card.setBorder(BorderFactory.createEmptyBorder(17, 20, 17, 20));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(LEFT_ALIGNMENT);

        JPanel dateCol = new JPanel();
        dateCol.setLayout(new BoxLayout(dateCol, BoxLayout.Y_AXIS));
        dateCol.setOpaque(false);
        dateCol.setPreferredSize(new Dimension(48, 60));

        String dayText = "—";
        String monthText = "Date unavailable";

        if (watchedDate != null && !watchedDate.isBlank()) {
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(watchedDate);
                dayText = String.valueOf(date.getDayOfMonth());
                monthText = date.getMonth().toString().substring(0, 3)
                        + " " + date.getYear();
            } catch (java.time.format.DateTimeParseException ignored) {
                monthText = watchedDate;
            }
        }

        JLabel dayLabel = new JLabel(dayText, JLabel.CENTER);
        dayLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        dayLabel.setForeground(ThemeManager.ACCENT_GOLD);
        dayLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel monthLabel = new JLabel(monthText, JLabel.CENTER);
        monthLabel.setFont(ThemeManager.FONT_SECONDARY);
        monthLabel.setForeground(ThemeManager.TEXT_MUTED);
        monthLabel.setAlignmentX(CENTER_ALIGNMENT);

        dateCol.add(dayLabel);
        dateCol.add(monthLabel);

        JPanel infoCol = new JPanel();
        infoCol.setLayout(new BoxLayout(infoCol, BoxLayout.Y_AXIS));
        infoCol.setOpaque(false);

        JLabel titleLabel = new JLabel(movie.title());
        titleLabel.setFont(ThemeManager.FONT_BUTTON);
        titleLabel.setForeground(ThemeManager.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metaRow.setOpaque(false);
        metaRow.setAlignmentX(LEFT_ALIGNMENT);
        metaRow.add(BadgeLabel.createRatingBadge(movie.rating()));
        metaRow.add(new BadgeLabel(movie.getGenresFormatted()));

        infoCol.add(titleLabel);
        infoCol.add(Box.createVerticalStrut(6));
        infoCol.add(metaRow);

        card.add(dateCol, BorderLayout.WEST);
        card.add(infoCol, BorderLayout.CENTER);
        return card;
    }

    // ─── Collections Tab ─────────────────────────────────────────────────────

    private void refreshFavouritesTab() {
        favouritesContainer.removeAll();
        favouritesContainer.add(createMovieGrid(repository.getFavouriteMovies(), onMovieSelected), BorderLayout.CENTER);
        favouritesContainer.revalidate();
        favouritesContainer.repaint();
    }

    private void refreshActivityTab() {
        activityContainer.removeAll();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        java.util.List<ActivityEvent> events = repository.getRecentActivity();
        if (events == null || events.isEmpty()) {
            JLabel empty = new JLabel("Your cinema activity will appear here.");
            empty.setForeground(ThemeManager.TEXT_MUTED);
            empty.setFont(ThemeManager.FONT_METADATA);
            content.add(empty);
        } else {
            for (ActivityEvent event : events) {
                JPanel item = new JPanel(new BorderLayout(8, 4));
                item.setOpaque(false);
                item.setBorder(BorderFactory.createEmptyBorder(14, 8, 14, 8));

                String titleText = event.movieTitle() == null ? "Cinema activity" : event.movieTitle();
                JLabel title = new JLabel(titleText);
                title.setForeground(ThemeManager.TEXT_PRIMARY);
                title.setFont(ThemeManager.FONT_SECTION_HEADING);

                String action = event.action() == null ? "UPDATED" : event.action().replace("_", " ");
                String date = event.date() == null ? "" : event.date();
                String details = event.details() == null ? "" : event.details();
                JLabel meta = new JLabel(action + "  •  " + date + (details.isBlank() ? "" : "  •  " + details));
                meta.setForeground(ThemeManager.TEXT_MUTED);
                meta.setFont(ThemeManager.FONT_METADATA);

                item.add(title, BorderLayout.NORTH);
                item.add(meta, BorderLayout.CENTER);
                content.add(item);
            }
        }

        activityContainer.add(ThemeManager.createStyledScrollPane(content), BorderLayout.CENTER);
        activityContainer.revalidate();
        activityContainer.repaint();
    }

    private void refreshCollectionsTab() {
        collectionsContainer.removeAll();
        collectionsContainer.add(createCollectionsTab(repository, onMovieSelected), BorderLayout.CENTER);
        collectionsContainer.revalidate();
        collectionsContainer.repaint();
    }

    private JPanel createCollectionsTab(CinemaRepository repository, Consumer<Movie> onMovieSelected) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(ThemeManager.SPACING_LG, 0, 0, 0));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        // 1. User Curated Custom Watchlists Section
        List<CustomWatchlist> customWatchlists = repository.getCustomWatchlists();
        if (!customWatchlists.isEmpty()) {
            JLabel customHeading = new JLabel("Curated Collections");
            customHeading.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(Font.BOLD, 20f));
            customHeading.setForeground(ThemeManager.TEXT_PRIMARY);
            customHeading.setAlignmentX(LEFT_ALIGNMENT);
            listPanel.add(customHeading);
            listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));

            for (CustomWatchlist wl : customWatchlists) {
                List<Movie> wlMovies = repository.getMoviesForCustomWatchlist(wl.getId());
                if (!wlMovies.isEmpty()) {
                    String title = wl.getName() + " (" + wl.getPrivacyLabel() + ")";
                    listPanel.add(buildCollectionSection(title, wlMovies, onMovieSelected));
                    listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));
                }
            }
        }

        // 2. Smart Genre Collections
        List<Movie> allPersonal = new ArrayList<>();
        allPersonal.addAll(repository.getWatchedMovies());
        allPersonal.addAll(repository.getWatchlist());

        if (!allPersonal.isEmpty()) {
            Map<String, List<Movie>> byGenre = allPersonal.stream()
                    .filter(m -> !m.genres().isEmpty())
                    .flatMap(m -> m.genres().stream().limit(2).map(g -> Map.entry(g, m)))
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

            if (!byGenre.isEmpty()) {
                JLabel genreHeading = new JLabel("Smart Genre Collections");
                genreHeading.setFont(ThemeManager.FONT_SECTION_HEADING.deriveFont(Font.BOLD, 20f));
                genreHeading.setForeground(ThemeManager.TEXT_PRIMARY);
                genreHeading.setAlignmentX(LEFT_ALIGNMENT);
                listPanel.add(genreHeading);
                listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));

                byGenre.entrySet().stream()
                        .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                        .forEach(entry -> {
                            listPanel.add(buildCollectionSection(entry.getKey(), entry.getValue(), onMovieSelected));
                            listPanel.add(Box.createVerticalStrut(ThemeManager.SPACING_XL));
                        });
            }
        }

        if (customWatchlists.isEmpty() && allPersonal.isEmpty()) {
            panel.add(ThemeManager.createEmptyState("📁", "No collections yet",
                    "Create custom watchlists or save movies to build automatic and curated collections."), BorderLayout.CENTER);
            return panel;
        }

        panel.add(ThemeManager.createStyledScrollPane(listPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCollectionSection(String title, List<Movie> movies, Consumer<Movie> onMovieSelected) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.SPACING_SM, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel genreLabel = new JLabel(title);
        genreLabel.setFont(ThemeManager.FONT_SECTION_HEADING);
        genreLabel.setForeground(ThemeManager.TEXT_PRIMARY);

        headerRow.add(genreLabel);
        headerRow.add(new BadgeLabel(movies.size() + " films"));
        section.add(headerRow);
        section.add(Box.createVerticalStrut(ThemeManager.SPACING_SM));

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, ThemeManager.CARD_GAP, 0));
        cardsRow.setOpaque(false);
        for (Movie m : movies) {
            cardsRow.add(new MovieCard(m, onMovieSelected));
        }

        JScrollPane rowScroll = new JScrollPane(cardsRow);
        rowScroll.setBorder(null);
        rowScroll.getViewport().setBackground(ThemeManager.BG_PRIMARY);
        rowScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        rowScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        rowScroll.getHorizontalScrollBar().setUnitIncrement(32);
        rowScroll.getHorizontalScrollBar().setBlockIncrement(160);
        rowScroll.setPreferredSize(new Dimension(Integer.MAX_VALUE, 330));
        rowScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 330));
        rowScroll.setAlignmentX(LEFT_ALIGNMENT);

        rowScroll.addMouseWheelListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                javax.swing.JScrollBar hbar = rowScroll.getHorizontalScrollBar();
                int move = e.getWheelRotation() * hbar.getUnitIncrement() * 3;
                hbar.setValue(hbar.getValue() + move);
                e.consume();
            }
        });
        ScrollWheelForwarder.installOn(cardsRow, rowScroll);

        section.add(rowScroll);
        return section;
    }

    private static class TabIcon implements javax.swing.Icon {
        enum Type { WATCHED, WATCHLIST, DIARY, COLLECTIONS }

        private final Type type;
        private final int size = 16;

        public TabIcon(Type type) {
            this.type = type;
        }

        @Override
        public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());

            switch (type) {
                case WATCHED -> {
                    g2.setStroke(new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 2, y + 8, x + 6, y + 13);
                    g2.drawLine(x + 6, y + 13, x + 14, y + 3);
                }
                case WATCHLIST -> {
                    g2.drawRoundRect(x + 2, y + 2, 12, 12, 3, 3);
                    g2.drawLine(x + 5, y + 6, x + 11, y + 6);
                    g2.drawLine(x + 5, y + 9, x + 9, y + 9);
                }
                case DIARY -> {
                    g2.drawRoundRect(x + 3, y + 1, 10, 14, 2, 2);
                    g2.drawLine(x + 5, y + 4, x + 10, y + 4);
                    g2.drawLine(x + 5, y + 7, x + 10, y + 7);
                    g2.drawLine(x + 5, y + 10, x + 8, y + 10);
                }
                case COLLECTIONS -> {
                    g2.drawRect(x + 1, y + 4, 14, 10);
                    g2.drawLine(x + 1, y + 4, x + 5, y + 1);
                    g2.drawLine(x + 5, y + 1, x + 10, y + 1);
                    g2.drawLine(x + 10, y + 1, x + 12, y + 4);
                }
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
