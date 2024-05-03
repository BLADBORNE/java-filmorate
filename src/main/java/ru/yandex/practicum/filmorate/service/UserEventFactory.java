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

    public static UserEvent getAddLike(int userId, int filmId) {
        return new UserEvent(userId, UserEvent.EventType.LIKE, UserEvent.OperationType.ADD, filmId);
    }

    public static UserEvent getDeleteLike(int userId, int filmId) {
        return new UserEvent(userId, UserEvent.EventType.LIKE, UserEvent.OperationType.REMOVE, filmId);
    }
}
