package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int usersId = 0;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(int id) {
        log.info(String.format("Получен запрос на отправку пользователя с id = %s", id));

        if (!users.containsKey(id)) {
            log.warn(String.format("Пользователь с id = %s отсутствует", id));

            throw new NoSuchElementException(String.format("Пользователь с id = %s отсутствует", id));
        }

        log.info(String.format("Пользователь с id = %s успешно отправлен клиенту", id));
        return users.get(id);
    }

    @Override
    public User createNewUser(User user) {
        log.info("Получен запрос на создание нового пользователя");

        user.setId(generateUserId());
        user.setUserFriends(new HashSet<>());
        setNicknameAsALoginIfANicknameIsEmpty(user);
        users.put(user.getId(), user);

        log.info("Пользователь с id = {} успешно создан", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("Получен запрос на обновление нового пользователя");

        if (!users.containsKey(user.getId())) {
            log.warn("Не можем обновить пользователя с id = {}, тк его нет в мапе", user.getId());

            throw new NoSuchElementException(String.format("Не можем обновить пользователя с id = %s, тк его нет", user.getId()));
        }

        setNicknameAsALoginIfANicknameIsEmpty(user);
        User oldUser = users.get(user.getId());
        user.setUserFriends(oldUser.getUserFriends());
        users.put(user.getId(), user);

        log.info("Пользователь с id = {} успешно обновлен", user.getId());

        return user;
    }

    private int generateUserId() {
        return ++usersId;
    }

    private void setNicknameAsALoginIfANicknameIsEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}