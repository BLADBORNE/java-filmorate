package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    public Film createNewFilm(Film film) {
        return filmStorage.createNewFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film deleteFilmById(int id) {
        return filmStorage.deleteFilmById(id);
    }

    public List<Film> getTopFilmsByLikes(int count) {
        return filmStorage.getTopFilmsByLikes(count);
    }

    public void addLikeToFilm(int filmId, int userId) {
        filmStorage.addLikeToFilm(filmId, userId);
    }

    public void deleteLikeFromFilm(int filmId, int userId) {
        filmStorage.deleteLikeFromFilm(filmId, userId);
    }

    public List<User> getFilmLikes(int id) {
        return filmStorage.getFilmLikes(id);
    }

    public Collection<Film> searchFilms(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }
}