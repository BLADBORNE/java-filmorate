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

    List<User> getUsersWhoScoredTheFilmById(int id);

    boolean getFilmScoreRecordByFilmIdUserIdAndScore(int filmId, int userId, int score);

    List<Film> getTopFilmsByScores(Integer count, Integer genreId, Integer year);

    List<Film> getTopCommonFilms(int userId1, int userId2);

    void addScoreToFilm(int filmId, int userId, int score);

    void deleteScoreFromFilm(int filmId, int userId);

    List<Film> getDirectorFilm(int directorId, String sortBy);

    List<Film> searchFilms(String query, String by);
}