package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.exception.ScoreValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping
    public List<Film> getFilms() {
        return service.getFilms();
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable(value = "filmId") Integer id) {
        return service.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getTopFilmsByScores(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "genreId", required = false) Integer genreId,
            @RequestParam(value = "year", required = false) Integer year
    ) {
        return service.getTopFilmsByScores(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getTopCommonsFilms(
            @RequestParam(value = "userId") Integer userId,
            @RequestParam(value = "friendId") Integer friendId
    ) {
        return service.getTopCommonFilms(userId, friendId);
    }

    @PutMapping("/{filmId}/score/{userId}")
    public void addScoreToFilm(
            @PathVariable(value = "filmId") Integer filmId,
            @PathVariable(value = "userId") Integer userId,
            @RequestParam(value = "score") Integer score
    ) {
        if (score == null) {
            throw new IllegalArgumentException("Оценка не может пуста, нужно передать число в диапозоне: [1; 10]");
        }

        if (score <= 0 || score >= 11) {
            log.warn("Клиент с id = {} передал неправильную оценку: - {}", userId, score);

            throw new ScoreValidationException(String.format("Оценка должна быть в диапозоне: [1; 10], ваша оценка - " +
                    "%d", score));
        }

        service.addScoreToFilm(filmId, userId, score);
    }

    @DeleteMapping("/{filmId}/score/{userId}")
    public void deleteScoreFromFilm(
            @PathVariable(value = "filmId") Integer filmId,
            @PathVariable(value = "userId") Integer userId
    ) {
        service.deleteScoreFromFilm(filmId, userId);
    }

    @PostMapping
    public Film createNewFilm(@Valid @RequestBody Film film) {
        checkDateValidation(film.getReleaseDate());

        return service.createNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        checkDateValidation(film.getReleaseDate());

        return service.updateFilm(film);
    }

    @DeleteMapping(value = {"", "/{filmId}"})
    public Film deleteFilmById(@PathVariable(value = "filmId", required = false) Optional<Integer> filmId) {
        if (filmId.isEmpty()) {
            throw new IllegalArgumentException("При удалении фильма не был передан id");
        }

        return service.deleteFilmById(filmId.get());
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getDirectorFilm(
            @PathVariable(value = "directorId") Integer directorId,
            @RequestParam(value = "sortBy") String sortBy) {
        return service.getDirectorFilm(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        return service.searchFilms(query, by);
    }

    private void checkDateValidation(LocalDate date) {
        if (date.isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("При создании фильма поле дата-релиза объекта Film не прошло валидацию");

            throw new DateValidationException("Дата фильма должна быть не меньше 1895-12-28");
        }
    }
}