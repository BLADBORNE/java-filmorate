package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidateTest {
    private UserController userController;
    private static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

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

    @Test
    public void shouldCreateAUserWithInvalidWrongEmail() {
        User user = User.builder()
                .email("@.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    public void shouldCreateAUserWithInvalidEmptyLogin() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    public void shouldCreateAUserWithInvalidWrongEmailAndInvalidEmptyLogin() {
        User user = User.builder()
                .email("@.com")
                .login("")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(2, violations.size());
    }
}