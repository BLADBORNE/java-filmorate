package ru.yandex.practicum.filmorate.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.rating.RatingStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContentBasedFilteringService {
    private UserStorage userStorage;
    private FilmStorage filmStorage;
    private RatingStorage ratingStorage;

    public List<Film> getRecommendationByGenre(Integer userId) {
        int numberOfFilms = filmStorage.getFilms().size();
        Set<Genre> genres = userStorage.getLikedFilmsId(userId).stream()
                .flatMap(id -> filmStorage.getFilmById(id).getGenres().stream())
                .collect(Collectors.toSet());

        return filmStorage.getTopFilmsByLikes(numberOfFilms).stream()
                .filter(film -> {
                    List<Genre> genresOfFilm = film.getGenres();
                    for (Genre genre : genres) {
                        if (genresOfFilm.contains(genre)) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
    }

    public List<Film> getPopularRecommendation() {
        int numberOfFilms = filmStorage.getFilms().size();

        List<Film> popular = filmStorage.getTopFilmsByLikes(numberOfFilms);

        if (filmStorage.getFilmLikes(popular.get(0).getId()).isEmpty()) {
            Collections.shuffle(popular);
            return popular;
        }

        return popular;
    }
}
