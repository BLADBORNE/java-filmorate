package ru.yandex.practicum.filmorate.storage.dao.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    Genre getGenreById(int id);

    List<Genre> getGenres();

    List<Genre> getFilmsGenres(int id);

    void updateFilmGenres(Film film);
}
