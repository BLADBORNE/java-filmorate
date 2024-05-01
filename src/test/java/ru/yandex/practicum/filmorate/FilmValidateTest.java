package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmValidateTest {
    private final FilmController filmController;
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
}