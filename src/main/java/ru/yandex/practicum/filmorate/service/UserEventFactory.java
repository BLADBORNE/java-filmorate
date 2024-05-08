package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.UserEvent;

public class UserEventFactory {

    private UserEventFactory() {

    }

    public static UserEvent getAddFriendEvent(int userId, int friendId) {
        return new UserEvent(userId, UserEvent.EventType.FRIEND, UserEvent.OperationType.ADD, friendId);
    }

    public static UserEvent getDeleteFriendEvent(int userId, int friendId) {
        return new UserEvent(userId, UserEvent.EventType.FRIEND, UserEvent.OperationType.REMOVE, friendId);
    }

    public static UserEvent getAddFilmLikeEvent(int userId, int filmId) {
        return new UserEvent(userId, UserEvent.EventType.SCORE, UserEvent.OperationType.ADD, filmId);
    }

    public static UserEvent getDeleteFilmLikeEvent(int userId, int filmId) {
        return new UserEvent(userId, UserEvent.EventType.SCORE, UserEvent.OperationType.REMOVE, filmId);
    }

    public static UserEvent getAddReviewEvent(int userId, int reviewId) {
        return new UserEvent(userId, UserEvent.EventType.REVIEW, UserEvent.OperationType.ADD, reviewId);
    }

    public static UserEvent getDeleteReviewEvent(int userId, int reviewId) {
        return new UserEvent(userId, UserEvent.EventType.REVIEW, UserEvent.OperationType.REMOVE, reviewId);
    }

    public static UserEvent getUpdateReviewEvent(int userId, int reviewId) {
        return new UserEvent(userId, UserEvent.EventType.REVIEW, UserEvent.OperationType.UPDATE, reviewId);
    }
}