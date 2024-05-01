package ru.yandex.practicum.filmorate.storage.dao.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FilmStorage {
    List<Film> getFilms();

    Film getFilmById(int id);

    Film createNewFilm(Film film);

    Film updateFilm(Film film);

    Film deleteFilmById(int id);

    List<User> getFilmLikes(int id);

    List<Film> getTopFilmsByLikes(int count);

    void addLikeToFilm(int filmId, int userId);

    void deleteLikeFromFilm(int filmId, int userId);

    List<Film> getDirectorFilm(int directorId, String sortBy);
}