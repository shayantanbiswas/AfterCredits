package com.aftercredits.repository;

import com.aftercredits.model.CustomWatchlist;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central state and persistence manager for watchlists (both default watchlist and custom watchlists).
 * Persists watchlist data to JSON and notifies registered UI listeners on modifications.
 */
public class WatchlistManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final WatchlistManager INSTANCE = new WatchlistManager();

    private final File storageDirectory;
    private File storageFile;
    private String activeUserKey = "legacy";
    private final Set<String> defaultWatchlistIds = new LinkedHashSet<>();
    private final List<CustomWatchlist> customWatchlists = new ArrayList<>();
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    public static WatchlistManager getInstance() {
        return INSTANCE;
    }

    private WatchlistManager() {
        File dir = new File(System.getProperty("user.dir"), ".aftercredits");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.canWrite()) {
            dir = new File(System.getProperty("java.io.tmpdir"), ".aftercredits");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        this.storageDirectory = dir;
        this.storageFile = new File(dir, "watchlists.json");
        load(true);
    }

    public synchronized void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public synchronized void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("[WatchlistManager] Error notifying listener: " + e.getMessage());
            }
        }
    }

    // ─── Default Watchlist ───────────────────────────────────────────────────

    public synchronized Set<String> getDefaultWatchlistIds() {
        return new LinkedHashSet<>(defaultWatchlistIds);
    }

    public synchronized boolean isDefaultWatchlisted(String movieId) {
        return movieId != null && defaultWatchlistIds.contains(movieId);
    }

    public synchronized boolean addToDefaultWatchlist(String movieId) {
        if (movieId != null && defaultWatchlistIds.add(movieId)) {
            save();
            notifyListeners();
            return true;
        }
        return false;
    }

    public synchronized boolean removeFromDefaultWatchlist(String movieId) {
        if (movieId != null && defaultWatchlistIds.remove(movieId)) {
            save();
            notifyListeners();
            return true;
        }
        return false;
    }

    // ─── Custom Watchlists ───────────────────────────────────────────────────

    public synchronized List<CustomWatchlist> getCustomWatchlists() {
        return Collections.unmodifiableList(new ArrayList<>(customWatchlists));
    }

    public synchronized Optional<CustomWatchlist> getCustomWatchlist(String id) {
        if (id == null) return Optional.empty();
        return customWatchlists.stream().filter(w -> w.getId().equals(id)).findFirst();
    }

    public synchronized CustomWatchlist createCustomWatchlist(String name, String description, boolean isPrivate) {
        String id = "wl-" + UUID.randomUUID().toString().substring(0, 8);
        CustomWatchlist watchlist = new CustomWatchlist(id, name, description, isPrivate);
        customWatchlists.add(watchlist);
        save();
        notifyListeners();
        return watchlist;
    }

    public synchronized boolean deleteCustomWatchlist(String id) {
        boolean removed = customWatchlists.removeIf(w -> w.getId().equals(id));
        if (removed) {
            save();
            notifyListeners();
        }
        return removed;
    }

    public synchronized boolean addMovieToCustomWatchlist(String watchlistId, String movieId) {
        Optional<CustomWatchlist> wlOpt = getCustomWatchlist(watchlistId);
        if (wlOpt.isPresent() && movieId != null) {
            boolean added = wlOpt.get().addMovie(movieId);
            if (added) {
                save();
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean removeMovieFromCustomWatchlist(String watchlistId, String movieId) {
        Optional<CustomWatchlist> wlOpt = getCustomWatchlist(watchlistId);
        if (wlOpt.isPresent() && movieId != null) {
            boolean removed = wlOpt.get().removeMovie(movieId);
            if (removed) {
                save();
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    public synchronized boolean setCustomWatchlistPrivacy(String watchlistId, boolean isPrivate) {
        Optional<CustomWatchlist> wlOpt = getCustomWatchlist(watchlistId);
        if (wlOpt.isPresent()) {
            wlOpt.get().setPrivate(isPrivate);
            save();
            notifyListeners();
            return true;
        }
        return false;
    }

    public synchronized List<CustomWatchlist> getWatchlistsContainingMovie(String movieId) {
        if (movieId == null) return Collections.emptyList();
        List<CustomWatchlist> result = new ArrayList<>();
        for (CustomWatchlist wl : customWatchlists) {
            if (wl.containsMovie(movieId)) {
                result.add(wl);
            }
        }
        return result;
    }

    public synchronized boolean isMovieInAnyWatchlist(String movieId) {
        if (movieId == null) return false;
        if (defaultWatchlistIds.contains(movieId)) return true;
        return customWatchlists.stream().anyMatch(w -> w.containsMovie(movieId));
    }

    // ─── Persistence ─────────────────────────────────────────────────────────

    private static class WatchlistStorageData {
        Set<String> defaultWatchlist;
        List<CustomWatchlistData> customWatchlists;
    }

    private static class CustomWatchlistData {
        String id;
        String name;
        String description;
        boolean isPrivate;
        List<String> movieIds;
        long createdAt;
    }

    /**
     * Switches local library storage to an account-specific file.
     * The legacy shared file is deliberately retained unchanged.
     */
    public synchronized void setActiveUserKey(String userKey) {
        if (userKey == null || userKey.isBlank()) return;

        String safeKey = userKey.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (safeKey.equals(activeUserKey)) return;

        save();
        activeUserKey = safeKey;
        storageFile = new File(storageDirectory, "watchlists-" + safeKey + ".json");
        load(false);

        System.out.println("[AfterCredits] Personal watchlist storage selected.");
        notifyListeners();
    }

    private synchronized void load(boolean seedIfEmpty) {
        defaultWatchlistIds.clear();
        customWatchlists.clear();

        if (storageFile.exists() && storageFile.length() > 0) {
            try (Reader reader = new FileReader(storageFile, StandardCharsets.UTF_8)) {
                Type dataType = new TypeToken<WatchlistStorageData>() {}.getType();
                WatchlistStorageData data = GSON.fromJson(reader, dataType);
                if (data != null) {
                    if (data.defaultWatchlist != null) {
                        defaultWatchlistIds.addAll(data.defaultWatchlist);
                    }
                    if (data.customWatchlists != null) {
                        for (CustomWatchlistData cwd : data.customWatchlists) {
                            customWatchlists.add(new CustomWatchlist(
                                    cwd.id,
                                    cwd.name,
                                    cwd.description,
                                    cwd.isPrivate,
                                    cwd.movieIds,
                                    cwd.createdAt
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[WatchlistManager] Could not read watchlists file: " + e.getMessage());
            }
        }

        // Seed initial data if newly launched
        if (seedIfEmpty && defaultWatchlistIds.isEmpty() && customWatchlists.isEmpty()) {
            seedDefaults();
            save();
        }
    }

    private void seedDefaults() {
        defaultWatchlistIds.add("mov-1"); // Blade Runner 2049
        defaultWatchlistIds.add("mov-5"); // Dune: Part Two

        CustomWatchlist sciFi = new CustomWatchlist(
                "wl-scifi",
                "Cyberpunk & Sci-Fi Visions",
                "Mind-bending dystopian futures, artificial intelligence, and philosophical space epics.",
                false, // Public
                new ArrayList<>(List.of("mov-1", "mov-3")),
                System.currentTimeMillis() - 86400000L * 3
        );

        CustomWatchlist weekend = new CustomWatchlist(
                "wl-weekend",
                "Weekend Cinema Marathon",
                "Personal queue for intense Friday & Saturday night movie sessions.",
                true, // Private
                new ArrayList<>(List.of("mov-5", "mov-2")),
                System.currentTimeMillis() - 86400000L
        );

        customWatchlists.add(sciFi);
        customWatchlists.add(weekend);
    }

    private synchronized void save() {
        try {
            WatchlistStorageData data = new WatchlistStorageData();
            data.defaultWatchlist = new LinkedHashSet<>(defaultWatchlistIds);
            data.customWatchlists = new ArrayList<>();
            for (CustomWatchlist wl : customWatchlists) {
                CustomWatchlistData cwd = new CustomWatchlistData();
                cwd.id = wl.getId();
                cwd.name = wl.getName();
                cwd.description = wl.getDescription();
                cwd.isPrivate = wl.isPrivate();
                cwd.movieIds = new ArrayList<>(wl.getMovieIds());
                cwd.createdAt = wl.getCreatedAt();
                data.customWatchlists.add(cwd);
            }

            try (Writer writer = new FileWriter(storageFile, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.err.println("[WatchlistManager] Failed to persist watchlists: " + e.getMessage());
        }
    }
}

