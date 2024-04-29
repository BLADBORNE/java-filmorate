package ru.yandex.practicum.filmorate.storage.dao.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    List<User> getUsers();

    User getUserById(int id);

    User createNewUser(User user);

    User updateUser(User user);

    User deleteUserById(int id);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);

    List<User> getUsersFriends(int userId);

    List<User> getCommonFriends(int userId, int otherId);

    List<Integer> getLikedFilmsId(Integer userId);
}