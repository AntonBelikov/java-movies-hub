package ru.practicum.moviehub.model;

import java.util.Objects;

public class Movie {
    private String title;
    private int year;
    private int id;

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
        id = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return year == movie.year && Objects.equals(title, movie.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year);
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year=" + year + '\'' +
                ", id=" + id +
                '}';
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setId(int id) {
        this.id = id;
    }
}