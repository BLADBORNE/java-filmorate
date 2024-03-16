package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Repository
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmsId = 0;

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(int id) {
        log.info(String.format("Получен запрос на отправку фильма с id = %s", id));

        if (!films.containsKey(id)) {
            log.warn(String.format("Отсутствует фильм с id = %s", id));

            throw new NoSuchElementException(String.format("Отсутствует фильм с id = %s", id));
        }

        log.info(String.format("Фильм с id = %s успешно отправлен клиенту", id));

        return films.get(id);
    }

    @Override
    public Film createNewFilm(Film film) {
        log.info("Получен запрос на создание нового фильма");

        checkTheFilmsBirthdayDate(film.getReleaseDate());

        film.setId(generateId());
        film.setFilmLikes(new HashSet<>());
        films.put(film.getId(), film);

        log.info("Фильм с id = {} успешно создан", film.getId());

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновление нового фильма");

        checkTheFilmsBirthdayDate(film.getReleaseDate());

        if (!films.containsKey(film.getId())) {
            log.warn("Не можем обновить фильм с id = {}, тк его нет в мапе", film.getId());

            throw new NoSuchElementException(String.format("Не можем обновить фильм с id = %s, тк его нет", film.getId()));
        }

        Film olfFilm = films.get(film.getId());
        film.setFilmLikes(olfFilm.getFilmLikes());
        films.put(film.getId(), film);

        log.info("Фильм с id = {} успешно обновлен", film.getId());

        return film;
    }

    @Override
    public Film deleteFilmById(int id) {
        log.info(String.format("Получен запрос на удаление фильма с id = %s", id));

        if (!films.containsKey(id)) {
            log.warn(String.format("Не можем удалаить фильм с id = %s, тк его нет", id));

            throw new NoSuchElementException(String.format("Не можем удалаить фильм с id = %s, тк его нет", id));
        }

        Film deletedFilm = getFilmById(id);

        films.remove(id);

        log.info(String.format("Фильм %s был успешно удален", deletedFilm.getName()));

        return deletedFilm;
    }

    private int generateId() {
        return ++filmsId;
    }

    private void checkTheFilmsBirthdayDate(LocalDate date) {
        if (date.isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("При создании фильма поле дата-релиза объекта Film не прошло валидацию");

            throw new ValidationException("При создании фильма объект не прошел валидацию");
        }
    }
}