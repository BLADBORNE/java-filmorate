package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {
    private FilmController filmController;

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
}

