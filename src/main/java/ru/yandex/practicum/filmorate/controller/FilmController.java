package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
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

    @PutMapping("/{filmId}/like/{userId}")
    public void addLikeToFilm(
            @PathVariable(value = "filmId") Integer filmId,
            @PathVariable(value = "userId") Integer userId) {
        service.addLikeToFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLikeFromFilm(
            @PathVariable(value = "filmId") Integer filmId,
            @PathVariable(value = "userId") Integer userId) {
        service.deleteLikeFromFilm(filmId, userId);
    }

    @PostMapping
    public Film createNewFilm(@Valid @RequestBody Film film) {
        return service.createNewFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
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
}