package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;

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

    public List<Film> getTopFilmsByScores(Integer count, Integer genreId, Integer year) {
        return filmStorage.getTopFilmsByScores(count, genreId, year);
    }

    public void addScoreToFilm(int filmId, int userId, int score) {
        filmStorage.addScoreToFilm(filmId, userId, score);
    }

    public void deleteScoreFromFilm(int filmId, int userId) {
        filmStorage.deleteScoreFromFilm(filmId, userId);
    }

    public List<User> getFilmLikes(int id) {
        return filmStorage.getFilmLikes(id);
    }

    public List<Film> getDirectorFilm(int directorId, String sortBy) {
        return filmStorage.getDirectorFilm(directorId, sortBy);
    }

    public List<Film> searchFilms(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    public List<Film> getTopCommonFilms(int userId1, int userId2) {
        return filmStorage.getTopCommonFilms(userId1, userId2);
    }
}