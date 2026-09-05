package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            // "/movies" -> ["", "movies"]; "/movies/5" -> ["", "movies", "5"]
            String[] pathParts = exchange.getRequestURI().getPath().split("/");

            switch (method) {
                case "GET":
                    if (pathParts.length == 2) {
                        handleGetAll(exchange);
                    } else if (pathParts.length == 3) {
                        handleGetById(exchange, pathParts[2]);
                    } else {
                        sendError(exchange, "Некорректный путь запроса", 404);
                    }
                    break;
                case "POST":
                    if (pathParts.length == 2) {
                        handlePost(exchange);
                    } else {
                        sendError(exchange, "POST поддерживается только для /movies", 405);
                    }
                    break;
                case "DELETE":
                    if (pathParts.length == 3) {
                        handleDelete(exchange, pathParts[2]);
                    } else {
                        sendError(exchange, "Для удаления укажите id: DELETE /movies/{id}", 405);
                    }
                    break;
                default:
                    sendError(exchange, "Метод " + method + " не поддерживается", 405);
            }
        } finally {
            exchange.close();
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        List<Movie> result;

        if (query != null && query.startsWith("year=")) {
            String yearParam = query.substring("year=".length());
            try {
                int year = Integer.parseInt(yearParam);
                result = store.getByYear(year);
            } catch (NumberFormatException e) {
                sendError(exchange, "Параметр year должен быть числом", 400);
                return;
            }
        } else {
            result = store.getAll();
        }

        sendText(exchange, gson.toJson(result), 200);
    }

    private void handleGetById(HttpExchange exchange, String idPart) throws IOException {
        Integer id = parseId(idPart);
        if (id == null) {
            sendError(exchange, "Некорректный id: " + idPart, 400);
            return;
        }

        Optional<Movie> movie = store.findById(id);
        if (movie.isPresent()) {
            sendText(exchange, gson.toJson(movie.get()), 200);
        } else {
            sendError(exchange, "Фильм с id=" + id + " не найден", 404);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Movie movie;
        try {
            movie = gson.fromJson(body, Movie.class);
        } catch (Exception e) {
            sendError(exchange, "Некорректный JSON в теле запроса", 400);
            return;
        }

        if (movie == null || movie.getTitle() == null || movie.getTitle().isBlank()) {
            sendError(exchange, "Поле title обязательно", 400);
            return;
        }

        Movie created = store.add(movie);
        sendText(exchange, gson.toJson(created), 201);
    }

    private void handleDelete(HttpExchange exchange, String idPart) throws IOException {
        Integer id = parseId(idPart);
        if (id == null) {
            sendError(exchange, "Некорректный id: " + idPart, 400);
            return;
        }

        if (store.deleteById(id)) {
            sendText(exchange, "", 200);
        } else {
            sendError(exchange, "Фильм с id=" + id + " не найден", 404);
        }
    }

    private Integer parseId(String idPart) {
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
