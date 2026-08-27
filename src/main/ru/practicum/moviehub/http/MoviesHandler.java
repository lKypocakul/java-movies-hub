package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;
    private final Gson gson = new Gson();

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("GET".equals(method)) {
                handleGet(exchange);
            } else {
                String body = gson.toJson(new ErrorResponse("Метод " + method + " не поддерживается"));
                sendText(exchange, body, 405);
            }
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String body = gson.toJson(store.getAll());
        sendText(exchange, body, 200);
    }
}
