package ru.yandex.practicum.filmorate.exception;

public class ScoreValidationException extends RuntimeException {
    public ScoreValidationException(String message) {
        super(message);
    }
}