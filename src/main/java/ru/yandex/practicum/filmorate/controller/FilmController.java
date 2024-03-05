package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmsId = 0;

    private int generateId() {
        return ++filmsId;
    }

    private void checkTheFilmsBirthdayDate(LocalDate date) {
        if (date.isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("При создании фильма поле дата-релиза объекта Film не прошло валидацию");
            throw new ValidationException("При создании фильма объект не прошел валидацию");
        }
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film createNewFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание нового фильма");

        checkTheFilmsBirthdayDate(film.getReleaseDate());

        film.setId(generateId());
        films.put(film.getId(), film);

        log.info("Фильм с id = {} успешно создан", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление нового фильма");

        checkTheFilmsBirthdayDate(film.getReleaseDate());

        if (!films.containsKey(film.getId())) {
            log.info("Не можем обновить фильм с id = {}, тк его нет в мапе", film.getId());
            throw new NoSuchElementException();
        }

        films.put(film.getId(), film);
        log.info("Фильм с id = {} успешно обновлен", film.getId());

        return film;
    }
}