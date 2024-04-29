package ru.yandex.practicum.filmorate.storage.dao.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {
    List<Director> getDirectors();

    Director getDirectorById(int id);

    List<Director> getFilmsDirectors(int id);

    Director createNewDirector(Director director);

    Director updateNewDirector(Director director);

    void updateFilmDirectors(Film film);

    void addDirectorsToFilm(Film film, List<Integer> addedFilmDirectors);

    void deleteFilmDirectors(Film film, List<Integer> removedFilmDirectors);
}
