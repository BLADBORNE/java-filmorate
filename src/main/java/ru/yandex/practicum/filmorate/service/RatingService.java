package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.dao.film.rating.RatingStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingStorage ratingStorage;

    public Rating getRatingById(int id) {
        return ratingStorage.getRatingById(id);
    }

    public List<Rating> getRatings() {
        return ratingStorage.getRatings();
    }
}
