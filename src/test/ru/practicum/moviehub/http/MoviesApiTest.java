package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) //
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void postMovieAndGetMoviesNotEmptyArray() throws Exception {
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Люди в черном\", \"year\": \"2018\"}"))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp1.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp1.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Мстители\", \"year\": \"2013\"}"))
                .build();

        HttpResponse<String> resp3 = client.send(req3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) //
                .GET()
                .build();

        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp2.statusCode(), "GET /movies должен вернуть 200");

        String body = resp2.body().trim();

        assertFalse(body.isBlank());
        assertEquals("[Movie{title='Люди в черном', year=2018'," +
                " id=2}, Movie{title='Мстители', year=2013', id=3}]", body);
    }

    @Test
    void postErrors() throws Exception {
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"\", \"year\": \"2018\"}"))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp1.statusCode(), "POST /movies должен вернуть 422");

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": " +
                        "\"adwdawdawdawdwadawdawdawdwdawdawdawdawdawadwdawdawdawdwadawdawdawdwdawdawdawdawdawadwdawd" +
                        "awdawdwadawdawdawdwdawdawdawdawdawadwdawdawdawdwadawdawdawdwdawddawdawdawadwdawdawdawdwadaw" +
                        "awfawfawfawfawfafawcawcawcavawvawvawvawvawvawvawvawvawvawvavwawawdawdawdwdawdawdawdawdaw\"," +
                        " \"year\": \"2018\"}"))
                .build();

        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp2.statusCode(), "POST /movies должен вернуть 422");

        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Люди в черном\", \"year\": \"2028\"}"))
                .build();

        HttpResponse<String> resp3 = client.send(req3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp3.statusCode(), "POST /movies должен вернуть 422");

        HttpRequest req4 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Люди в черном\", \"year\": \"2020\"}"))
                .build();

        HttpResponse<String> resp4 = client.send(req4, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp4.statusCode(), "POST /movies должен вернуть 415");

        HttpRequest req5 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Movie1\", \"year\":2023"))
                .build();

        HttpResponse<String> resp5 = client.send(req5, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp5.statusCode(), "POST /movies должен вернуть 400");
    }

    @Test
    void testGetByIdAndDelete() throws Exception {

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"Приключения Шурика\",\"year\":\"1978\"}"))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp1.statusCode(), "POST /movies должен вернуть 201");

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + 1))
                .GET()
                .build();

        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp2.statusCode(), "GET /movies должен вернуть 200");

        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + 1))
                .DELETE()
                .build();

        HttpResponse<String> resp3 = client.send(req3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp3.statusCode(), "DELETE /movies должен вернуть 204");
    }

    @Test
    void testGetByIdAndDeleteErrors() throws Exception {

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + 18080))
                .GET()
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp1.statusCode(), "GET /movies должен вернуть 404");

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + "abcd"))
                .GET()
                .build();

        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp2.statusCode(), "GET /movies должен вернуть 400");

        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + 12392))
                .DELETE()
                .build();

        HttpResponse<String> resp3 = client.send(req3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp3.statusCode(), "DELETE /movies должен вернуть 404");

        HttpRequest req4 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + "abcd"))
                .DELETE()
                .build();

        HttpResponse<String> resp4 = client.send(req4, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp4.statusCode(), "DELETE /movies должен вернуть 400");
    }

    @Test
    void testGetByYearAndErrors() throws Exception {
        HttpRequest reqPost = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"12 стульев\",\"year\":\"1975\"}"))
                .build();

        HttpResponse<String> respPost = client.send(reqPost, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies" + "?year=1975"))
                .GET()
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp1.statusCode(), "GET /movies должен вернуть 200");

        String body = resp1.body().trim();

        assertFalse(body.isBlank());
        assertEquals("[Movie{title='12 стульев', year=1975', id=4}]", body);

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies" + "?year=2000"))
                .GET()
                .build();

        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp2.statusCode(), "GET /movies должен вернуть 404");

        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies" + "?year=2adf"))
                .GET()
                .build();

        HttpResponse<String> resp3 = client.send(req3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp3.statusCode(), "GET /movies должен вернуть 400");

        HttpRequest reqDelete = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + 1))
                .DELETE()
                .build();

        HttpResponse<String> respDelete =
                client.send(reqDelete, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}