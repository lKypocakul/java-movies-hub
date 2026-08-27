package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new LinkedHashMap<>();

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    public Optional<Movie> findById(int id) {
        return Optional.ofNullable(movies.get(id));
    }

    public void add(Movie movie) {
        movies.put(movie.getId(), movie);
    }

    public boolean deleteById(int id) {
        return movies.remove(id) != null;
    }

    public void clear() {
        movies.clear();
    }
}
