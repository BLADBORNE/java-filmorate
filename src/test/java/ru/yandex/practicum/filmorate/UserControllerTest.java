package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private UserController userController;

    @BeforeEach
    public void createNewUserController() {
        userController = new UserController();
    }

    @Test
    public void shouldThrownAnExceptionIfUserDateIsFromTheFeature() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2120, 3, 5))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> userController.createNewUser(user));
        assertEquals("При создании пользователя объект не прошел валидацию", exception.getMessage());
    }

    @Test
    public void shouldCreateNewUserIfUserBirthdayEqualsCurrentDay() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userController.createNewUser(user);
        assertEquals(1, userController.getUsers().size());
        assertTrue(userController.getUsers().contains(user));
    }
}
