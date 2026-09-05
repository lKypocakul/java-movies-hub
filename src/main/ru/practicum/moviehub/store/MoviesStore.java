package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new LinkedHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public List<Movie> getAll() {
        return new ArrayList<>(movies.values());
    }

    public List<Movie> getByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.getReleaseYear() != null && movie.getReleaseYear() == year)
                .collect(Collectors.toList());
    }

    public Optional<Movie> findById(int id) {
        return Optional.ofNullable(movies.get(id));
    }

    public Movie add(Movie movie) {
        int id = idCounter.incrementAndGet();
        movie.setId(id);
        movies.put(id, movie);
        return movie;
    }

    public boolean deleteById(int id) {
        return movies.remove(id) != null;
    }

    public void clear() {
        movies.clear();
        idCounter.set(0);
    }
}
