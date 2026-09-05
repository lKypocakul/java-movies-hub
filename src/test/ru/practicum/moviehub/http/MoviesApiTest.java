package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    private static final String BASE_URL = "http://localhost:8080/movies";

    private static MoviesStore store;
    private static MoviesServer server;
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpResponse<String> response = get(BASE_URL);

        assertEquals(200, response.statusCode());
        List<Movie> movies = gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType());
        assertTrue(movies.isEmpty());
    }

    @Test
    void postMovie_withValidBody_createsMovieAndReturns201() throws Exception {
        Movie newMovie = new Movie(null, "Начало", "Фильм про сны", 2010, "Фантастика");

        HttpResponse<String> response = post(BASE_URL, gson.toJson(newMovie));

        assertEquals(201, response.statusCode());
        Movie created = gson.fromJson(response.body(), Movie.class);
        assertNotNull(created.getId());
        assertEquals("Начало", created.getTitle());
    }

    @Test
    void postMovie_withoutTitle_returns400() throws Exception {
        Movie invalid = new Movie(null, null, "Без названия", 2020, "Драма");

        HttpResponse<String> response = post(BASE_URL, gson.toJson(invalid));

        assertEquals(400, response.statusCode());
        ErrorResponse error = gson.fromJson(response.body(), ErrorResponse.class);
        assertNotNull(error.getMessage());
    }

    @Test
    void getMovieById_whenExists_returnsMovie() throws Exception {
        Movie created = createMovie("Матрица", 1999, "Фантастика");

        HttpResponse<String> response = get(BASE_URL + "/" + created.getId());

        assertEquals(200, response.statusCode());
        Movie found = gson.fromJson(response.body(), Movie.class);
        assertEquals(created.getId(), found.getId());
        assertEquals("Матрица", found.getTitle());
    }

    @Test
    void getMovieById_whenNotExists_returns404() throws Exception {
        HttpResponse<String> response = get(BASE_URL + "/999");

        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteMovie_whenExists_returns200AndRemovesMovie() throws Exception {
        Movie created = createMovie("Аватар", 2009, "Фантастика");

        HttpResponse<String> deleteResponse = delete(BASE_URL + "/" + created.getId());
        assertEquals(200, deleteResponse.statusCode());

        HttpResponse<String> getResponse = get(BASE_URL + "/" + created.getId());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void deleteMovie_whenNotExists_returns404() throws Exception {
        HttpResponse<String> response = delete(BASE_URL + "/999");

        assertEquals(404, response.statusCode());
    }

    @Test
    void getMoviesByYear_returnsOnlyMatchingMovies() throws Exception {
        createMovie("Матрица", 1999, "Фантастика");
        createMovie("Начало", 2010, "Фантастика");
        createMovie("Терминатор 2", 1991, "Фантастика");

        HttpResponse<String> response = get(BASE_URL + "?year=1999");

        assertEquals(200, response.statusCode());
        List<Movie> movies = gson.fromJson(response.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(1, movies.size());
        assertEquals("Матрица", movies.get(0).getTitle());
    }

    private Movie createMovie(String title, int year, String genre) throws Exception {
        Movie movie = new Movie(null, title, "", year, genre);
        HttpResponse<String> response = post(BASE_URL, gson.toJson(movie));
        return gson.fromJson(response.body(), Movie.class);
    }

    private HttpResponse<String> get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
