package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Review {
    private int reviewId;
    private String content;
    private boolean isPositive;
    private int userId;
    private int filmId;
    private int useful;
}
