package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.recommendation.RecommedationService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RecommedationService recommedationService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable(value = "userId", required = false) int userId) {
        return userService.getUserById(userId);
    }

    @PostMapping
    public User createNewUser(@Valid @RequestBody User user) {
        return userService.createNewUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable(value = "id") int userId,
            @PathVariable(value = "friendId") int friendId
    ) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable(value = "id") int userId,
            @PathVariable(value = "friendId") int friendId
    ) {
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUsersFriends(
            @PathVariable(value = "id") int userId
    ) {
        return userService.getUsersFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable(value = "id") int userId,
            @PathVariable(value = "otherId") int otherId
    ) {
        return userService.getCommonFriends(userId, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable(value = "id") int userId) {
        return recommedationService.getRecommendation(userId);
    }

    @DeleteMapping(value = {"", "/{userId}"})
    public User deleteUserById(
            @PathVariable(value = "userId", required = false) Optional<Integer> userId
    ) {
        if (userId.isEmpty()) {
            throw new IllegalArgumentException("При удалении пользователя не был передан id");
        }

        return userService.deleteUserById(userId.get());
    }
}