package ru.yandex.practicum.filmorate.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.exception.ScoreValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmValidateTest {
    private final Map<Integer, String> ratings = Map.of(1, "G", 2, "PG");
    private final Map<Integer, String> genres = Map.of(1, "Комедия", 2, "Драма");
    private final FilmController filmController;
    private final UserService userService;
    private final FilmService filmService;
    private static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void shouldThrownAnExceptionIfFilmsDateIsBeforeTheFilmsBirthdayDate() {
        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(200)
                .build();
        DateValidationException exception = assertThrows(DateValidationException.class, () ->
                filmController.createNewFilm(film));

        assertEquals("Дата фильма должна быть не меньше 1895-12-28", exception.getMessage());
    }

    @Test
    public void shouldCreateNewFilmIfFilmsDateEqualsTheFilmsBirthdayDate() {
        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
    }

    @Test
    public void shouldCreateAFilmWithInvalidEmptyName() {
        Film film = Film.builder()
                .name("")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    public void shouldCreateAFilmWithInvalidNegativeDuration() {
        Film film = Film.builder()
                .name("Ilya")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(-1)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    public void shouldCreateAFilmWithInvalidEmptyNameAndInvalidNegativeDuration() {
        Film film = Film.builder()
                .name("")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(-1)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(2, violations.size());
    }

    @Test
    public void shouldThrownAnExceptionIfScoreEquals0And11AndMinusOne() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        User createdUser = userService.createNewUser(user);

        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        Film createdFilm = filmService.createNewFilm(film);


        assertThrows(ScoreValidationException.class, () ->
                filmController.addScoreToFilm(createdUser.getId(), createdFilm.getId(), 0));

        assertThrows(ScoreValidationException.class, () ->
                filmController.addScoreToFilm(createdUser.getId(), createdFilm.getId(), 11));

        assertThrows(ScoreValidationException.class, () ->
                filmController.addScoreToFilm(createdUser.getId(), createdFilm.getId(), -1));
    }
}