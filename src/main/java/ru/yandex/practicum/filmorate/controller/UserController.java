package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int usersId = 0;

    public int generateUserId() {
        return ++usersId;
    }

    private void checkThatUserDateIsNotFromTheFeature(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            log.error("При создании пользователя поле дня рождения не прошло валидацию");
            throw new ValidationException("При создании пользователя объект не прошел валидацию");
        }
    }

    private void setNicknameAsALoginIfANicknameIsEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание нового пользователя");

        checkThatUserDateIsNotFromTheFeature(user.getBirthday());

        setNicknameAsALoginIfANicknameIsEmpty(user);

        user.setId(generateUserId());
        users.put(user.getId(), user);

        log.info("Пользователь с id = {} успешно создан", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на обновление нового пользователя");

        checkThatUserDateIsNotFromTheFeature(user.getBirthday());

        setNicknameAsALoginIfANicknameIsEmpty(user);

        if (!users.containsKey(user.getId())) {
            log.info("Не можем обновить пользователя с id = {}, тк его нет в мапе", user.getId());
            throw new NoSuchElementException();
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id = {} успешно обновлен", user.getId());

        return user;
    }
}