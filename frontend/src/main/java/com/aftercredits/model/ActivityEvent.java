package com.aftercredits.model;

public record ActivityEvent(
        String action,
        String movieId,
        String movieTitle,
        String date,
        String details
) {}
