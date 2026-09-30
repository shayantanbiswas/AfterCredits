package com.aftercredits.model;

import java.util.Objects;

/**
 * Represents a community film club or cinephile group.
 */
public record MovieGroup(
        String id,
        String name,
        String focusGenre,
        int memberCount,
        String currentDiscussionMovie,
        String description,
        boolean isJoined
) {
    public MovieGroup {
        Objects.requireNonNull(id, "Group ID must not be null");
        Objects.requireNonNull(name, "Group name must not be null");
        Objects.requireNonNull(focusGenre, "Focus genre must not be null");
        Objects.requireNonNull(description, "Description must not be null");
    }

    /**
     * Creates a new MovieGroup instance with updated membership state.
     * Keeps records immutable while allowing state transitions.
     */
    public MovieGroup withJoined(boolean newJoinedState) {
        int updatedCount = newJoinedState ? (memberCount + 1) : Math.max(0, memberCount - 1);
        return new MovieGroup(id, name, focusGenre, updatedCount, currentDiscussionMovie, description, newJoinedState);
    }
}
