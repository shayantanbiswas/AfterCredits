package com.aftercredits.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a custom user-curated watchlist.
 * Supports private and public visibility options and dynamic movie additions/removals.
 */
public class CustomWatchlist {

    private final String id;
    private String name;
    private String description;
    private boolean isPrivate;
    private final List<String> movieIds;
    private final long createdAt;

    public CustomWatchlist(String id, String name, String description, boolean isPrivate) {
        this(id, name, description, isPrivate, new ArrayList<>(), System.currentTimeMillis());
    }

    public CustomWatchlist(
            String id,
            String name,
            String description,
            boolean isPrivate,
            List<String> movieIds,
            long createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Watchlist ID must not be null");
        this.name = (name == null || name.isBlank()) ? "Untitled Watchlist" : name.trim();
        this.description = (description == null) ? "" : description.trim();
        this.isPrivate = isPrivate;
        this.movieIds = (movieIds == null) ? new ArrayList<>() : new ArrayList<>(movieIds);
        this.createdAt = (createdAt > 0) ? createdAt : System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        this.isPrivate = aPrivate;
    }

    public String getPrivacyLabel() {
        return isPrivate ? "🔒 Private" : "🌐 Public";
    }

    public List<String> getMovieIds() {
        return Collections.unmodifiableList(movieIds);
    }

    public int getMovieCount() {
        return movieIds.size();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean containsMovie(String movieId) {
        return movieId != null && movieIds.contains(movieId);
    }

    public boolean addMovie(String movieId) {
        if (movieId != null && !movieIds.contains(movieId)) {
            movieIds.add(movieId);
            return true;
        }
        return false;
    }

    public boolean removeMovie(String movieId) {
        return movieIds.remove(movieId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomWatchlist that = (CustomWatchlist) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

