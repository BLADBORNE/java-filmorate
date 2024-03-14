package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final InMemoryUserStorage userStorage;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public User createNewUser(User user) {
        return userStorage.createNewUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public void addFriend(int userId, int friendId) {
        log.info(String.format("Получен запрос на добавление в друзья. Пользователь с id = %s хочет добавить " +
                "пользователя с id = %s", userId, friendId));

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        Set<Integer> userFriends = user.getUserFriends();
        Set<Integer> friendsOfAFriend = friend.getUserFriends();

        userFriends.add(friendId);
        friendsOfAFriend.add(userId);

        log.info(String.format("Пользователи %s и %s стали друзьями", user.getName(), friend.getName()));
    }

    public void deleteFriend(int userId, int friendId) {
        log.info(String.format("Получен запрос на удаление из друзей. Пользователь с id = %s хочет удалить друга " +
                "с id = %s", userId, friendId));

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        Set<Integer> userFriends = user.getUserFriends();
        Set<Integer> friendsOfAFriend = friend.getUserFriends();

        userFriends.remove(friendId);
        friendsOfAFriend.remove(userId);

        log.info(String.format("Пользователи %s и %s больше не друзья", user.getName(), friend.getName()));
    }

    public List<User> getUsersFriends(int userId) {
        log.info(String.format("Получен запрос на отправку друзей пользователя с id = %s", userId));

        User user = userStorage.getUserById(userId);

        log.info(String.format("Пользователю %s успешно отправлены его друзья", user.getName()));

        return userStorage.getUsers().stream()
                .filter(u -> user.getUserFriends().contains(u.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.info(String.format("Получен запрос на отправку общих друзей пользователей с id = %s и с id = %s ", userId,
                otherId));

        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherId);

        user.getUserFriends().retainAll(otherUser.getUserFriends());

        log.info(String.format("Список общих друзей пользователей %s и %s успешно отправлен", user.getName(),
                otherUser.getName()));

        return userStorage.getUsers().stream()
                .filter(u -> user.getUserFriends().contains(u.getId()))
                .collect(Collectors.toList());
    }
}