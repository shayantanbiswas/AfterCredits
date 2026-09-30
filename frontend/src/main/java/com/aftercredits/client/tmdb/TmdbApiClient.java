package com.aftercredits.client.tmdb;

import com.aftercredits.model.Movie;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP client for The Movie Database (TMDB) API v3.
 * Authenticates using the TMDB v3 API Key passed as a query parameter (api_key=...).
 * The key is read securely from the environment and is never logged or exposed.
 * Maps raw JSON responses into domain Movie records.
 */
public class TmdbApiClient {

    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final Map<Integer, String> GENRE_MAP = Map.ofEntries(
            Map.entry(28, "Action"),
            Map.entry(12, "Adventure"),
            Map.entry(16, "Animation"),
            Map.entry(35, "Comedy"),
            Map.entry(80, "Crime"),
            Map.entry(99, "Documentary"),
            Map.entry(18, "Drama"),
            Map.entry(10751, "Family"),
            Map.entry(14, "Fantasy"),
            Map.entry(36, "History"),
            Map.entry(27, "Horror"),
            Map.entry(10402, "Music"),
            Map.entry(9648, "Mystery"),
            Map.entry(10749, "Romance"),
            Map.entry(878, "Sci-Fi"),
            Map.entry(10770, "TV Movie"),
            Map.entry(53, "Thriller"),
            Map.entry(10752, "War"),
            Map.entry(37, "Western")
    );

    private final String apiKey;
    private final HttpClient httpClient;

    public TmdbApiClient(String apiKey) {
        this.apiKey = (apiKey == null) ? "" : apiKey.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Checks whether an API key is configured.
     */
    public boolean hasApiKey() {
        return !apiKey.isBlank();
    }

    /**
     * Backward-compatible alias for hasApiKey().
     */
    public boolean hasToken() {
        return hasApiKey();
    }

    /**
     * Constructs a full TMDB request URL with the v3 api_key query parameter appended.
     * Package-private for unit testing request construction without network calls.
     */
    String buildRequestUrl(String endpointWithParams) {
        char delimiter = endpointWithParams.contains("?") ? '&' : '?';
        return TMDB_BASE_URL + endpointWithParams + delimiter + "api_key=" + apiKey;
    }

    /**
     * Searches TMDB for movies matching the given query string.
     * Supports international and classic cinema (e.g. Satyajit Ray, Alfred Hitchcock).
     */
    public List<Movie> searchMovies(String query) throws Exception {
        if (!hasApiKey() || query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String url = buildRequestUrl("/search/movie?query=" + encodedQuery + "&include_adult=false&language=en-US&page=1");

        String json = executeGet(url);
        return parseSearchResponse(json);
    }

    /**
     * Fetches top trending movies of the week.
     */
    public List<Movie> getTrendingMovies() throws Exception {
        if (!hasApiKey()) {
            return Collections.emptyList();
        }

        String url = buildRequestUrl("/trending/movie/week?language=en-US");
        String json = executeGet(url);
        return parseSearchResponse(json);
    }

    /**
     * Fetches full movie details including credits (director, cast) and external IDs (IMDb).
     */
    public Optional<Movie> getMovieDetails(int tmdbId) {
        if (!hasApiKey() || tmdbId <= 0) {
            return Optional.empty();
        }

        try {
            String url = buildRequestUrl("/movie/" + tmdbId + "?append_to_response=credits,external_ids&language=en-US");
            String json = executeGet(url);
            return Optional.ofNullable(parseMovieDetail(json));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String executeGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("Accept", "application/json")
                .header("User-Agent", "AfterCredits-Desktop/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else if (response.statusCode() == 401) {
            throw new IllegalStateException("TMDB API Authentication failed (HTTP 401). Verify TMDB_V3_API_KEY.");
        } else {
            throw new RuntimeException("TMDB API responded with HTTP " + response.statusCode());
        }
    }

    /**
     * Package-private JSON parsing helper for search responses, accessible for unit tests.
     */
    List<Movie> parseSearchResponse(String jsonString) {
        List<Movie> results = new ArrayList<>();
        if (jsonString == null || jsonString.isBlank()) {
            return results;
        }

        JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
        if (!root.has("results") || !root.get("results").isJsonArray()) {
            return results;
        }

        JsonArray array = root.getAsJsonArray("results");
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject obj = element.getAsJsonObject();

            int tmdbId = getInt(obj, "id", 0);
            if (tmdbId <= 0) continue;

            String title = getString(obj, "title", "Untitled");
            String originalTitle = getString(obj, "original_title", title);
            String releaseDate = getString(obj, "release_date", "");
            int year = parseYear(releaseDate);

            double voteAvg = getDouble(obj, "vote_average", 0.0);
            int voteCount = getInt(obj, "vote_count", 0);
            String synopsis = getString(obj, "overview", "No synopsis available.");
            String posterPath = getNullableString(obj, "poster_path");
            String backdropPath = getNullableString(obj, "backdrop_path");

            // Extract genres from genre_ids
            List<String> genres = new ArrayList<>();
            if (obj.has("genre_ids") && obj.get("genre_ids").isJsonArray()) {
                for (JsonElement gId : obj.getAsJsonArray("genre_ids")) {
                    String genreName = GENRE_MAP.get(gId.getAsInt());
                    if (genreName != null && !genres.contains(genreName)) {
                        genres.add(genreName);
                    }
                }
            }

            results.add(new Movie(
                    "tmdb-" + tmdbId,
                    tmdbId,
                    title,
                    originalTitle,
                    year,
                    releaseDate,
                    "Not loaded", // Director will populate on detail query
                    List.of(),
                    genres,
                    voteAvg,
                    voteAvg,
                    voteCount,
                    0, // Runtime loaded in details
                    synopsis,
                    "",
                    posterPath,
                    backdropPath,
                    null,
                    false // Live TMDB record
            ));
        }

        return results;
    }

    /**
     * Package-private JSON parsing helper for detail responses, accessible for unit tests.
     */
    Movie parseMovieDetail(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return null;
        }

        JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
        int tmdbId = getInt(root, "id", 0);
        String title = getString(root, "title", "Untitled");
        String originalTitle = getString(root, "original_title", title);
        String releaseDate = getString(root, "release_date", "");
        int year = parseYear(releaseDate);
        int runtime = getInt(root, "runtime", 0);
        double voteAvg = getDouble(root, "vote_average", 0.0);
        int voteCount = getInt(root, "vote_count", 0);
        String synopsis = getString(root, "overview", "No synopsis available.");
        String tagline = getString(root, "tagline", "");
        String posterPath = getNullableString(root, "poster_path");
        String backdropPath = getNullableString(root, "backdrop_path");

        // Parse genres array
        List<String> genres = new ArrayList<>();
        if (root.has("genres") && root.get("genres").isJsonArray()) {
            for (JsonElement g : root.getAsJsonArray("genres")) {
                if (g.isJsonObject()) {
                    genres.add(getString(g.getAsJsonObject(), "name", ""));
                }
            }
        }

        // Parse credits for director & top billed cast
        String director = "Unknown Director";
        List<String> cast = new ArrayList<>();

        if (root.has("credits") && root.get("credits").isJsonObject()) {
            JsonObject credits = root.getAsJsonObject("credits");

            // Extract Director from crew
            if (credits.has("crew") && credits.get("crew").isJsonArray()) {
                for (JsonElement c : credits.getAsJsonArray("crew")) {
                    JsonObject crewMember = c.getAsJsonObject();
                    String job = getString(crewMember, "job", "");
                    if ("Director".equalsIgnoreCase(job)) {
                        director = getString(crewMember, "name", "Unknown Director");
                        break;
                    }
                }
            }

            // Extract Top 6 Cast
            if (credits.has("cast") && credits.get("cast").isJsonArray()) {
                JsonArray castArray = credits.getAsJsonArray("cast");
                for (int i = 0; i < Math.min(6, castArray.size()); i++) {
                    JsonObject actor = castArray.get(i).getAsJsonObject();
                    String actorName = getString(actor, "name", "");
                    if (!actorName.isBlank()) {
                        cast.add(actorName);
                    }
                }
            }
        }

        // Parse external IDs (IMDb)
        String imdbId = null;
        if (root.has("external_ids") && root.get("external_ids").isJsonObject()) {
            imdbId = getNullableString(root.getAsJsonObject("external_ids"), "imdb_id");
        }

        return new Movie(
                "tmdb-" + tmdbId,
                tmdbId,
                title,
                originalTitle,
                year,
                releaseDate,
                director,
                cast,
                genres,
                voteAvg,
                voteAvg,
                voteCount,
                runtime,
                synopsis,
                tagline,
                posterPath,
                backdropPath,
                imdbId,
                false // Live TMDB record
        );
    }

    private static int parseYear(String dateStr) {
        if (dateStr != null && dateStr.length() >= 4) {
            try {
                return Integer.parseInt(dateStr.substring(0, 4));
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static String getString(JsonObject obj, String member, String fallback) {
        if (obj.has(member) && !obj.get(member).isJsonNull()) {
            return obj.get(member).getAsString();
        }
        return fallback;
    }

    private static String getNullableString(JsonObject obj, String member) {
        if (obj.has(member) && !obj.get(member).isJsonNull()) {
            String val = obj.get(member).getAsString();
            return val.isBlank() ? null : val;
        }
        return null;
    }

    private static int getInt(JsonObject obj, String member, int fallback) {
        if (obj.has(member) && !obj.get(member).isJsonNull()) {
            return obj.get(member).getAsInt();
        }
        return fallback;
    }

    private static double getDouble(JsonObject obj, String member, double fallback) {
        if (obj.has(member) && !obj.get(member).isJsonNull()) {
            return obj.get(member).getAsDouble();
        }
        return fallback;
    }
}
