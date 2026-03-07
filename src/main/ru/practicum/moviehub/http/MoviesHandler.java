package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            getByGet(ex);
        } else if (method.equalsIgnoreCase("POST")) {
            postNewFilm(ex);
        } else if (method.equalsIgnoreCase("DELETE")) {
            deleteFilmById(ex);
        } else {
            sendErrorJson(ex, 415,"Неверный тип метода", "Необходимо выбратьGET, POST, DELETE");
        }
    }

    private void getAllFilms(HttpExchange exchange) throws IOException {
        String response = moviesStore.getMovieList().toString();
        sendJson(exchange, 200, response);
    }

    private void postNewFilm(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

        if (!contentType.equals("application/json")) {
            sendErrorJson(exchange, 415,"Неверный тип заголовка", "Проверьте введенные данные");
            return;
        }

        byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        Gson gson = new Gson();
        Movie movie;

        try {
            movie = gson.fromJson(body, Movie.class);
        } catch (Exception e) {
            sendErrorJson(exchange, 400,
                    "Некорректный формат JSON", "Тело запроса не может быть преобразовано в JSON");
            return;
        }

        if (movie.getTitle().length() > 100) {
            sendErrorJson(exchange, 422,
                    "Введено название длиннее 100 символов", "Название должно быть не длиннее 100 символов");
        } else if (movie.getTitle() == null || movie.getTitle().isBlank()) {
            sendErrorJson(exchange, 422,
                    "Название фильма не указано или пустое", "Пожалуйста, укажите название фильма");
        } else if (movie.getYear() < 1888 || movie.getYear() > LocalDate.now().getYear() + 1) {
            sendErrorJson(exchange, 422,"Год должен быть между 1888 и 2027", "Проверьте правильность указанного года");
        } else {
            if (moviesStore.checkFilm(movie.hashCode())) {
                sendErrorJson(exchange, 409,"Фильм уже есть в базе", "Фильм был добавлен ранее");
            } else {
                moviesStore.addFilm(movie);
                sendJson(exchange, 201, movie.toString());
            }
        }
    }

    private void sendErrorJson(HttpExchange exchange, int statusCode, String error, String details) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error, details);
        String response = new Gson().toJson(errorResponse);
        sendJson(exchange, statusCode, response);
    }

    private void getFilmById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        Optional<Movie> filmId;

        try {
            filmId = moviesStore.findFilm(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            sendErrorJson(exchange, 400,"Некорректный ID", "Введите корректный ID из цифр");
            return;
        }

        if (filmId.isEmpty()) {
            sendErrorJson(exchange, 404,"Фильм не найден", "фильм с данным ID отсутствует в базе");
        } else {
            Movie movie = filmId.get();
            sendJson(exchange, 200, movie.toString());
        }
    }

    private void getByYear(HttpExchange exchange) throws IOException {
        String yearParts = exchange.getRequestURI().getQuery();
        int year;

        try {
            year = Integer.parseInt(yearParts.split("=")[1]);
        } catch (NumberFormatException exception) {
            sendErrorJson(exchange, 400, "Некорректный параметр запроса — 'year'", "Проверьте данные запроса");
            return;
        }

        if (year < 1888 || year > LocalDate.now().getYear() + 1) {
            sendErrorJson(exchange, 422,"Год должен быть между 1888 и 2027", "Проверьте правильность указанного года");
            return;
        }

        List<Movie> movieList = moviesStore.findFilmByYear(year);

        if (movieList.isEmpty()) {
            sendErrorJson(exchange, 404,"Фильмы за данный год не найдены", "Данный год еще не добавлен");
            return;
        }

        sendJson(exchange, 200, movieList.toString());
    }

    private void getByGet(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        if (pathParts.length == 2 && pathParts[1].equals("movies")) {

            if (exchange.getRequestURI().getQuery() != null && exchange.getRequestURI().getQuery().contains("year")) {
                getByYear(exchange);
                return;
            }

            getAllFilms(exchange);
        } else if (pathParts.length == 3 && pathParts[1].equals("movies")) {
            getFilmById(exchange);
        } else {
            sendErrorJson(exchange, 404,"Неверный путь запроса", "Проверье введенные данные");
        }
    }

    private void deleteFilmById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        int id;

        try {
            id = Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException exception) {
            sendErrorJson(exchange, 400,"Некорректный ID", "Проверье ID");
            return;
        }

        boolean deleteStatus = moviesStore.deleteFilm(id);

        if (deleteStatus) {
            sendNoContent(exchange);
        } else {
            sendErrorJson(exchange, 404,"Фильм с данным ID не найден", "Такого фильма еще нет");
        }
    }
}
