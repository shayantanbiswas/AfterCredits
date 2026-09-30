package com.aftercredits.repository;

import com.aftercredits.model.FilmRoomTopic;
import com.aftercredits.model.Movie;
import com.aftercredits.model.MovieGroup;
import com.aftercredits.model.Review;
import com.aftercredits.model.UserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * In-memory mock implementation of {@link CinemaRepository}.
 * Serves as an offline fallback containing realistic pre-seeded cinema data.
 * All records are explicitly flagged with isMock = true to distinguish them from live TMDB data.
 */
public class MockCinemaRepository implements CinemaRepository {

    private final List<Movie> movies = new ArrayList<>();
    private final Set<String> watchlistIds = new LinkedHashSet<>();
    private final Map<String, Double> watchedMovieRatings = new LinkedHashMap<>();
    private final List<FilmRoomTopic> filmRoomTopics = new ArrayList<>();
    private final Map<String, MovieGroup> movieGroups = new LinkedHashMap<>();
    private final List<Review> communityReviews = new ArrayList<>();
    private UserProfile userProfile;

    public MockCinemaRepository() {
        seedInitialData();
    }

    private void seedInitialData() {
        // --- 1. Seed Movie Catalog with Rich Real Metadata ---
        movies.add(new Movie(
                "mov-1",
                335984,
                "Blade Runner 2049",
                "Blade Runner 2049",
                2017,
                "2017-10-06",
                "Denis Villeneuve",
                List.of("Ryan Gosling", "Harrison Ford", "Ana de Armas", "Sylvia Hoeks"),
                List.of("Sci-Fi", "Neo-Noir", "Mystery"),
                8.5,
                8.0,
                13200,
                164,
                "A young blade runner's discovery of a long-buried secret leads him to track down former blade runner Rick Deckard, missing for thirty years.",
                "The key to the future is finally unearthed.",
                "/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg",
                "/ilRyAZstrM9zwGF2Pt9JMi09h7b.jpg",
                "tt1856101",
                true
        ));

        movies.add(new Movie(
                "mov-2",
                496243,
                "Parasite",
                "기생충",
                2019,
                "2019-05-30",
                "Bong Joon-ho",
                List.of("Song Kang-ho", "Lee Sun-kyun", "Cho Yeo-jeong", "Choi Woo-shik"),
                List.of("Thriller", "Drama", "Dark Comedy"),
                8.8,
                8.5,
                18100,
                132,
                "Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.",
                "Act like you own the place.",
                "/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
                "/hiKmpZMGZsrkA3cdce8a7Dpos1j.jpg",
                "tt6751668",
                true
        ));

        movies.add(new Movie(
                "mov-3",
                157336,
                "Interstellar",
                "Interstellar",
                2014,
                "2014-11-05",
                "Christopher Nolan",
                List.of("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine"),
                List.of("Sci-Fi", "Adventure", "Drama"),
                8.7,
                8.4,
                35200,
                169,
                "When Earth becomes uninhabitable, a team of researchers and an ex-pilot travel through a wormhole in search of a new home for mankind.",
                "Mankind was born on Earth. It was never meant to die here.",
                "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
                "tt0816692",
                true
        ));

        movies.add(new Movie(
                "mov-4",
                129,
                "Spirited Away",
                "千と千尋の神隠し",
                2001,
                "2001-07-20",
                "Hayao Miyazaki",
                List.of("Rumi Hiiragi", "Miyu Irino", "Mari Natsuki"),
                List.of("Animation", "Fantasy", "Adventure"),
                8.6,
                8.5,
                16500,
                125,
                "A sullen 10-year-old girl wanders into a mysterious spirit world ruled by gods and witches after her parents undergo a strange transformation.",
                "Nothing that happens is ever forgotten.",
                "/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg",
                "/bXNvzjYE9rvBA1atGhLOjlIG3Zs.jpg",
                "tt0245429",
                true
        ));

        movies.add(new Movie(
                "mov-5",
                693134,
                "Dune: Part Two",
                "Dune: Part Two",
                2024,
                "2024-03-01",
                "Denis Villeneuve",
                List.of("Timothée Chalamet", "Zendaya", "Rebecca Ferguson", "Javier Bardem"),
                List.of("Sci-Fi", "Adventure", "Action"),
                8.9,
                8.2,
                5600,
                166,
                "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.",
                "Long live the fighters.",
                "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                "/xOMo8BRK7PfcJv9JCnx7s520Wio.jpg",
                "tt15239678",
                true
        ));

        movies.add(new Movie(
                "mov-6",
                244786,
                "Whiplash",
                "Whiplash",
                2014,
                "2014-10-10",
                "Damien Chazelle",
                List.of("Miles Teller", "J.K. Simmons", "Paul Reiser"),
                List.of("Drama", "Music", "Psychological"),
                8.5,
                8.4,
                15000,
                106,
                "A promising jazz drummer enrolls at a cut-throat conservatory where a relentless instructor pushes him beyond his physical and mental limits.",
                "The road to greatness can take you to the edge.",
                "/7fn624j5lj3xTme2SgiLCeuedmO.jpg",
                "/6bbZ6XyvgfjhQwfplEdgvnFrBum.jpg",
                "tt2582802",
                true
        ));

        movies.add(new Movie(
                "mov-7",
                872585,
                "Oppenheimer",
                "Oppenheimer",
                2023,
                "2023-07-21",
                "Christopher Nolan",
                List.of("Cillian Murphy", "Emily Blunt", "Matt Damon", "Robert Downey Jr."),
                List.of("Biography", "Drama", "History"),
                8.8,
                8.1,
                9200,
                180,
                "The story of American theoretical physicist J. Robert Oppenheimer, the Manhattan Project director, and the haunting aftermath of the atomic bomb.",
                "The world forever changes.",
                "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                "/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg",
                "tt15398776",
                true
        ));

        movies.add(new Movie(
                "mov-8",
                11104,
                "Chungking Express",
                "重慶森林",
                1994,
                "1994-07-14",
                "Wong Kar-wai",
                List.of("Takeshi Kaneshiro", "Brigitte Lin", "Tony Leung Chiu-wai", "Faye Wong"),
                List.of("Romance", "Drama", "Arthouse"),
                8.1,
                8.0,
                1600,
                102,
                "Two melancholic Hong Kong policemen navigate heartbreak and fleeting romances amidst the neon bustle of Tsim Sha Tsui.",
                "If memories could be canned, would they also have an expiry date?",
                "/4XEU8f38WzL8z6sXgE2bNl5Y0eM.jpg",
                null,
                "tt0109424",
                true
        ));

        movies.add(new Movie(
                "mov-9",
                120467,
                "The Grand Budapest Hotel",
                "The Grand Budapest Hotel",
                2014,
                "2014-03-07",
                "Wes Anderson",
                List.of("Ralph Fiennes", "Tony Revolori", "Saoirse Ronan", "Willem Dafoe"),
                List.of("Comedy", "Adventure", "Drama"),
                8.1,
                8.0,
                14500,
                99,
                "A legendary concierge at a famed European ski resort and his junior lobby boy become entangled in the theft of a priceless Renaissance painting.",
                "A delightfully eccentric caper.",
                "/eWdyYQreja6JGCzqHWX9ne3rN45.jpg",
                null,
                "tt2278388",
                true
        ));

        movies.add(new Movie(
                "mov-10",
                11423,
                "Memories of Murder",
                "살인의 추억",
                2003,
                "2003-05-02",
                "Bong Joon-ho",
                List.of("Song Kang-ho", "Kim Sang-kyung", "Roe-ha Kim"),
                List.of("Crime", "Drama", "Mystery"),
                8.1,
                8.1,
                3800,
                131,
                "In 1986 rural South Korea, two mismatched detectives investigate a series of brutal, unsolved murders that test their sanity.",
                "The haunting pursuit of an elusive truth.",
                "/7f0Fk0c2lG4KjK0u9u1Q3eP6.jpg",
                null,
                "tt0353969",
                true
        ));

        // --- 2. Seed Personal Library (Watched & Watchlist) ---
        watchlistIds.add("mov-1"); // Blade Runner 2049
        watchlistIds.add("mov-8"); // Chungking Express
        watchlistIds.add("mov-9"); // The Grand Budapest Hotel

        watchedMovieRatings.put("mov-2", 4.8); // Parasite
        watchedMovieRatings.put("mov-3", 4.5); // Interstellar
        watchedMovieRatings.put("mov-4", 4.7); // Spirited Away
        watchedMovieRatings.put("mov-5", 5.0); // Dune: Part Two
        watchedMovieRatings.put("mov-6", 4.5); // Whiplash
        watchedMovieRatings.put("mov-7", 4.9); // Oppenheimer

        // --- 3. Seed Film Rooms (Movie-Specific Discussion Spaces with Spoiler Awareness) ---
        filmRoomTopics.add(new FilmRoomTopic(
                "topic-1",
                "mov-7",
                "Oppenheimer",
                "The Ending Scene: Einstein's Dialogue and the Metaphor of Chain Reactions",
                "Ending & Climax",
                "marcus_cine",
                "The final exchange between Oppenheimer and Einstein frames the entire political hearing as trivial compared to what they truly started. The realization that they *did* destroy the world is chilling.",
                true,
                24,
                142
        ));

        filmRoomTopics.add(new FilmRoomTopic(
                "topic-2",
                "mov-1",
                "Blade Runner 2049",
                "Roger Deakins' Lighting Architecture: Saturated Amber vs. Dystopian Mist",
                "Cinematography",
                "film_aesthetic",
                "Analyzing how Las Vegas ruins use oppressive yellow-amber haze while the Wallace corporation uses caustic reflective water caustics on clean concrete surfaces.",
                false,
                18,
                98
        ));

        filmRoomTopics.add(new FilmRoomTopic(
                "topic-3",
                "mov-2",
                "Parasite",
                "Architectural Verticality: Stairs, Basements, and Rain Flow as Class Symbolism",
                "Theories & Symbolism",
                "bong_analyst",
                "Notice how the Kim family descends hundreds of stairs during the monsoon rain sequence while water flows downward into the sub-basement. Every single vertical axis is deliberate.",
                true,
                31,
                215
        ));

        filmRoomTopics.add(new FilmRoomTopic(
                "topic-4",
                "mov-3",
                "Interstellar",
                "Miller's Planet: The Psychological Terror of Gravitational Time Dilation",
                "Story & Science",
                "astronomy_buff",
                "Each tick in the soundtrack represents 1.4 days on Earth passing by. The realization that every second cost them years with their families makes it one of cinema's most intense sequences.",
                false,
                15,
                174
        ));

        // --- 4. Seed Movie Groups ---
        movieGroups.put("grp-1", new MovieGroup(
                "grp-1",
                "Neo-Noir & Speculative Fiction",
                "Sci-Fi / Noir",
                1420,
                "Blade Runner 2049",
                "Analyzing dystopian cities, artificial consciousness, and moral ambiguity in speculative cinema.",
                true
        ));

        movieGroups.put("grp-2", new MovieGroup(
                "grp-2",
                "Criterion & Arthouse Society",
                "Arthouse / World Cinema",
                890,
                "Chungking Express",
                "Celebrating auteur theory, foreign language classics, restored 4K prints, and quiet masterworks.",
                false
        ));

        movieGroups.put("grp-3", new MovieGroup(
                "grp-3",
                "A24 & Contemporary Visions",
                "Indie / Psychological",
                2130,
                "Past Lives",
                "Exploring bold auteur voices, unconventional narratives, and indie masterpieces.",
                true
        ));

        movieGroups.put("grp-4", new MovieGroup(
                "grp-4",
                "East Asian Cinema Circle",
                "Crime / Drama / Thriller",
                760,
                "Memories of Murder",
                "Deep dives into Korean New Wave, classic Japanese cinema, and Hong Kong golden era films.",
                false
        ));

        // --- 5. Seed Community Reviews ---
        communityReviews.add(new Review(
                "rev-1",
                "mov-5",
                "Dune: Part Two",
                "sophia_films",
                5.0,
                "A gargantuan sensory achievement. Villeneuve has created a modern sci-fi milestone on par with The Empire Strikes Back.",
                "2 hours ago",
                false
        ));

        communityReviews.add(new Review(
                "rev-2",
                "mov-2",
                "Parasite",
                "david_critic",
                4.8,
                "The shift in genre halfway through remains one of the sharpest screenwriting turns in modern cinema history.",
                "Yesterday",
                true
        ));

        communityReviews.add(new Review(
                "rev-3",
                "mov-6",
                "Whiplash",
                "elena_rhythm",
                4.5,
                "The editing in the final drum solo creates more suspense than ninety percent of Hollywood action movies.",
                "3 days ago",
                false
        ));

        // --- 6. Seed User Profile with Film DNA ---
        Map<String, Integer> dnaPercentages = new LinkedHashMap<>();
        dnaPercentages.put("Sci-Fi", 35);
        dnaPercentages.put("Neo-Noir", 25);
        dnaPercentages.put("Psychological Thriller", 20);
        dnaPercentages.put("Arthouse / World", 12);
        dnaPercentages.put("Animation", 8);

        userProfile = new UserProfile(
                "cinephile_rahul",
                "@rahul_cine",
                "Auteur Specialist",
                "Passionate about slow-burn narratives, atmospheric neo-noir, and philosophical sci-fi. 70mm enthusiast.",
                284,
                42,
                16,
                95,
                dnaPercentages,
                List.of("Denis Villeneuve", "Bong Joon-ho", "Christopher Nolan", "Hayao Miyazaki")
        );
    }

    // --- Movie Catalog Implementations ---

    @Override
    public List<Movie> getAllMovies() {
        return Collections.unmodifiableList(movies);
    }

    @Override
    public Optional<Movie> getMovieById(String id) {
        return movies.stream().filter(m -> m.id().equals(id)).findFirst();
    }

    @Override
    public List<Movie> searchMovies(String query, String genreFilter) {
        String cleanQuery = (query == null) ? "" : query.trim().toLowerCase();
        String cleanGenre = (genreFilter == null) ? "All" : genreFilter.trim();

        return movies.stream()
                .filter(m -> {
                    boolean matchesQuery = cleanQuery.isEmpty()
                            || m.title().toLowerCase().contains(cleanQuery)
                            || m.director().toLowerCase().contains(cleanQuery);

                    boolean matchesGenre = cleanGenre.equalsIgnoreCase("All")
                            || m.genres().stream().anyMatch(g -> g.equalsIgnoreCase(cleanGenre));

                    return matchesQuery && matchesGenre;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Movie> getTrendingMovies() {
        return movies.stream()
                .sorted((a, b) -> Double.compare(b.rating(), a.rating()))
                .limit(6)
                .collect(Collectors.toList());
    }

    @Override
    public Movie getSpotlightMovie() {
        return getMovieById("mov-5").orElse(movies.getFirst());
    }

    // --- My Cinema Implementations ---

    @Override
    public List<Movie> getWatchedMovies() {
        return movies.stream()
                .filter(m -> watchedMovieRatings.containsKey(m.id()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Movie> getWatchlist() {
        return movies.stream()
                .filter(m -> watchlistIds.contains(m.id()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isWatchlisted(String movieId) {
        return watchlistIds.contains(movieId);
    }

    @Override
    public boolean isWatched(String movieId) {
        return watchedMovieRatings.containsKey(movieId);
    }

    @Override
    public void addToWatchlist(String movieId) {
        watchlistIds.add(movieId);
    }

    @Override
    public void removeFromWatchlist(String movieId) {
        watchlistIds.remove(movieId);
    }

    @Override
    public void logMovieAsWatched(String movieId, double userRating) {
        watchedMovieRatings.put(movieId, userRating);
        watchlistIds.remove(movieId);
    }

    // --- Film Rooms Implementations ---

    @Override
    public List<FilmRoomTopic> getAllFilmRoomTopics() {
        return Collections.unmodifiableList(filmRoomTopics);
    }

    @Override
    public List<FilmRoomTopic> getTopicsForMovie(String movieId) {
        return filmRoomTopics.stream()
                .filter(t -> t.movieId().equals(movieId))
                .collect(Collectors.toList());
    }

    @Override
    public void addFilmRoomTopic(FilmRoomTopic topic) {
        filmRoomTopics.add(0, topic);
    }

    // --- Movie Groups Implementations ---

    @Override
    public List<MovieGroup> getAllMovieGroups() {
        return new ArrayList<>(movieGroups.values());
    }

    @Override
    public void toggleGroupMembership(String groupId) {
        MovieGroup current = movieGroups.get(groupId);
        if (current != null) {
            movieGroups.put(groupId, current.withJoined(!current.isJoined()));
        }
    }

    // --- Community Reviews Implementations ---

    @Override
    public List<Review> getCommunityReviews() {
        return Collections.unmodifiableList(communityReviews);
    }

    @Override
    public void addReview(Review review) {
        communityReviews.add(0, review);
    }

    // --- User Profile & Film DNA Implementations ---

    @Override
    public UserProfile getUserProfile() {
        return userProfile;
    }
}
