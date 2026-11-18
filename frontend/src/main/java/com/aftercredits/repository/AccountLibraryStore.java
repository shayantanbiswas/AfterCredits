package com.aftercredits.repository;

import com.aftercredits.client.auth.SessionManager;
import com.aftercredits.model.Review;
import com.aftercredits.model.ActivityEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

public final class AccountLibraryStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Data {
        Map<String, Double> watched = new LinkedHashMap<>();
        Map<String, String> watchedDates = new LinkedHashMap<>();
        List<Review> reviews = new ArrayList<>();
        List<String> favourites = new ArrayList<>();
        List<ActivityEvent> activities = new ArrayList<>();
    }

    private final File directory;
    private String activeKey;
    private Data data = new Data();

    public AccountLibraryStore() {
        directory = new File(System.getProperty("user.dir"), ".aftercredits");
        if (!directory.exists()) directory.mkdirs();
    }

    private String currentKey() {
        SessionManager.Session session = SessionManager.getSession();

        if (session == null) return "local";
        if (session.isGuest() && session.getGuestId() != null)
            return "guest-" + session.getGuestId();
        if (session.getUserId() > 0)
            return "account-" + session.getUserId();

        return "local";
    }

    private synchronized void ensureLoaded() {
        String key = currentKey();
        if (key.equals(activeKey)) return;

        activeKey = key;
        data = new Data();

        File file = new File(directory, "library-" + key + ".json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            if (loaded != null) {
                data = loaded;
                if (data.watched == null) data.watched = new LinkedHashMap<>();
                if (data.reviews == null) data.reviews = new ArrayList<>();
                if (data.watchedDates == null) data.watchedDates = new LinkedHashMap<>();
                if (data.favourites == null) data.favourites = new ArrayList<>();
                if (data.activities == null) data.activities = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("[AfterCredits] Could not load personal library: " + e.getMessage());
        }
    }

    private synchronized void save() {
        ensureLoaded();
        File file = new File(directory, "library-" + activeKey + ".json");

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            System.err.println("[AfterCredits] Could not save personal library: " + e.getMessage());
        }
    }

    public synchronized Map<String, Double> getWatchedRatings() {
        ensureLoaded();
        return new LinkedHashMap<>(data.watched);
    }

    public synchronized void markWatched(String movieId, double rating) {
        ensureLoaded();
        data.watched.put(movieId, rating);
        data.watchedDates.put(movieId, LocalDate.now().toString());
        addActivity("WATCHED", movieId, movieId, "Logged as watched");
        save();
    }

    public synchronized String getWatchedDate(String movieId) {
        ensureLoaded();
        return data.watchedDates.getOrDefault(movieId, "");
    }

    public synchronized List<Review> getReviews() {
        ensureLoaded();
        return new ArrayList<>(data.reviews);
    }

    private void addActivity(String action, String movieId, String title, String details) {
        data.activities.add(0, new ActivityEvent(
                action, movieId, title, java.time.LocalDateTime.now().toString(), details
        ));
        if (data.activities.size() > 200) {
            data.activities = new ArrayList<>(data.activities.subList(0, 200));
        }
    }

    public synchronized List<String> getFavouriteIds() {
        ensureLoaded();
        return new ArrayList<>(data.favourites);
    }

    public synchronized boolean isFavourite(String movieId) {
        ensureLoaded();
        return data.favourites.contains(movieId);
    }

    public synchronized void toggleFavourite(String movieId, String title) {
        ensureLoaded();
        if (data.favourites.remove(movieId)) {
            addActivity("UNFAVOURITED", movieId, title, "Removed from favourites");
        } else {
            data.favourites.add(0, movieId);
            addActivity("FAVOURITED", movieId, title, "Added to favourites");
        }
        save();
    }

    public synchronized List<ActivityEvent> getActivities() {
        ensureLoaded();
        return new ArrayList<>(data.activities);
    }

    public synchronized void addReview(Review review) {
        ensureLoaded();
        data.reviews.removeIf(r -> r.id().equals(review.id()));
        data.reviews.add(0, review);
        addActivity("REVIEW", review.movieId(), review.movieTitle(),
                "Wrote a review · " + review.getFormattedRating());
        save();
    }
}
