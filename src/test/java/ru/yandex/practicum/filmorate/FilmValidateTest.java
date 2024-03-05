package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmValidateTest {
    private static Validator validator;
    private FilmController filmController;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    public void createNewFilmController() {
        filmController = new FilmController();
    }

    @Test
    public void shouldThrownAnExceptionIfFilmsDateIsBeforeTheFilmsBirthdayDate() {
        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(200)
                .build();
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.createNewFilm(film));

        assertEquals("При создании фильма объект не прошел валидацию", exception.getMessage());
    }

    @Test
    public void shouldCreateNewFilmIfFilmsDateEqualsTheFilmsBirthdayDate() {
        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        filmController.createNewFilm(film);
        assertEquals(1, filmController.getFilms().size());
        assertTrue(filmController.getFilms().contains(film));
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
}