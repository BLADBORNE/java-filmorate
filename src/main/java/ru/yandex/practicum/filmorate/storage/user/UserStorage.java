package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getUsers();

    User getUserById(int id);

    User createNewUser(User user);

    User updateUser(User user);

    User deleteUserById(int id);
}