package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

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
    public Film getFilmById(@PathVariable(value = "filmId") int id) {
        return service.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> getTopFilmsByLikes(
            @RequestParam(value = "count", defaultValue = "10", required = false) int count
    ) {
        return service.getTopFilmsByLikes(count);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLikeToFilm(
            @PathVariable(value = "filmId") int filmId,
            @PathVariable(value = "userId") int userId
    ) {
        service.addLikeToFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLikeFromFilm(
            @PathVariable(value = "filmId") int filmId,
            @PathVariable(value = "userId") int userId
    ) {
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
}