package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

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

    public List<Film> getTopFilmsByLikes(int count) {
        log.info(String.format("Получен запрос на получении топ %s лучших фильмов", count));
        log.info(String.format("Топ %s лучших фильмов отправлены клиенту", count));

        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(film -> film.getFilmLikes().size() * -1))
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLikeToFilm(int filmId, int userId) {
        log.info(String.format("Получен запрос на добавление лайка фильму с id = %s от пользователя  c id = %s",
                filmId, userId));

        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        Set<Integer> filmLikes = film.getFilmLikes();

        filmLikes.add(user.getId());

        log.info(String.format("Пользователь %s успешно поставил лайк фильму %s", user.getName(), film.getName()));
    }

    public void deleteLikeFromFilm(int filmId, int userId) {
        log.info(String.format("Получен запрос на удаление лайка фильму с id = %s от пользователя c id = %s", filmId,
                userId));

        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        Set<Integer> filmLikes = film.getFilmLikes();

        filmLikes.remove(user.getId());

        log.info(String.format("Пользователь %s успешно удалил лайк фильму %s", user.getName(), film.getName()));
    }
}