package ru.yandex.practicum.filmorate.exception;

public class ReviewsUsefulValidationException extends IllegalArgumentException {
    public ReviewsUsefulValidationException(String message) {
        super(message);
    }
}