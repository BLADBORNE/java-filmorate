package ru.yandex.practicum.filmorate.storage.dao.film.rating;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;

public interface RatingStorage {
    Rating getRatingById(int id);

    List<Rating> getRatings();
}
