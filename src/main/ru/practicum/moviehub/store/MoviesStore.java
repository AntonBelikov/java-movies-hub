package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MoviesStore {
    private List<Movie> movieList;

    public MoviesStore() {
        movieList = new ArrayList<>();
    }

    public Optional<Movie> findFilm(int id) {
        for (Movie movie : movieList) {
            if (movie.hashCode() == id) {
                return Optional.of(movie);
            }
        }
        return Optional.empty();
    }

    public void addFilm(Movie movie) {
        movieList.add(movie);
    }

    public boolean deleteFilm(int id) {
        for (Movie movie : movieList) {
            if (movie.hashCode() == id) {
                return movieList.remove(movie);
            }
        }
        return false;
    }

    public boolean checkFilm(int id) {
        for (Movie movie : movieList) {
            if (movie.hashCode() == id) {
                return true;
            }
        }
        return false;
    }

    public List<Movie> findFilmByYear(int year) {
        return movieList.stream().filter(movie -> movie.getYear() == year).collect(Collectors.toList());
    }

    public List<Movie> getMovieList() {
        return movieList;
    }
}