package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmDbStorageTest {
    private final Map<Integer, String> ratings = Map.of(1, "G", 2, "PG");
    private final Map<Integer, String> genres = Map.of(1, "Комедия", 2, "Драма");
    private final Map<Integer, String> directors = Map.of(1, "Тарантино");
    private final FilmService filmService;
    private final UserService userService;
    private final DirectorService directorService;

    @Test
    public void shouldCreateFilm() {
        Director director = directorService.createNewDirector(new Director(1, "Тарантино"));
        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .directors(List.of(director))
                .build();

        filmService.createNewFilm(film);

        Film filmFromBd = filmService.getFilmById(film.getId());

        assertNotNull(filmFromBd);
        assertEquals(1, filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(filmFromBd));
        assertEquals(film, filmFromBd);
    }

    @Test
    public void shouldUpdateFilm() {
        Director director = directorService.createNewDirector(new Director(2, "Нолан"));
        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .directors(List.of(director))
                .build();

        filmService.createNewFilm(film);

        Film filmFromBd = filmService.getFilmById(film.getId());

        assertNotNull(filmFromBd);
        assertEquals(1, filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(filmFromBd));
        assertEquals(film, filmFromBd);

        film.setName("UpdatedName");
        film.setGenres(List.of(new Genre(2, genres.get(2))));

        filmService.updateFilm(film);

        assertEquals(1,filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(film));
        assertFalse(filmService.getFilms().contains(filmFromBd));
    }

    @Test
    public void shouldDeleteFilmById() {
        Director director = directorService.createNewDirector(new Director(3, "Скорсезе"));
        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .directors(List.of(director))
                .build();

        filmService.createNewFilm(film);

        Film filmFromBd = filmService.getFilmById(film.getId());

        assertNotNull(filmFromBd);
        assertEquals(1, filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(filmFromBd));
        assertEquals(film, filmFromBd);

        filmService.deleteFilmById(filmFromBd.getId());

        assertEquals(0, filmService.getFilms().size());
        assertFalse(filmService.getFilms().contains(filmFromBd));
    }

    @Test
    public void addLikeToFilm() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userService.createNewUser(user);

        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        filmService.createNewFilm(film);

        filmService.addLikeToFilm(user.getId(), film.getId());

        assertEquals(1, filmService.getFilmLikes(film.getId()).size());
        assertTrue(filmService.getFilmLikes(film.getId()).contains(user));
    }

    @Test
    public void shouldDeleteLikeFromFilm() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userService.createNewUser(user);

        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        filmService.createNewFilm(film);

        filmService.addLikeToFilm(user.getId(), film.getId());

        assertEquals(1, filmService.getFilmLikes(film.getId()).size());
        assertTrue(filmService.getFilmLikes(film.getId()).contains(user));

        filmService.deleteLikeFromFilm(film.getId(), user.getId());

        assertEquals(0, filmService.getFilmLikes(film.getId()).size());
        assertFalse(filmService.getFilmLikes(film.getId()).contains(user));
    }

    @Test
    public void shouldGetTopFilmsByLikesAndTheThirdFilmMustBeOnTheFirstPlace() {
        User user1 = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        User user2 = User.builder()
                .email("iliashacool@gmail.com")
                .login("Maxim")
                .name("Max228")
                .birthday(LocalDate.of(2012, 12, 1))
                .build();

        User user3 = User.builder()
                .email("test12@gmail.com")
                .login("Anstasya")
                .name("Milo23")
                .birthday(LocalDate.of(2008, 12, 1))
                .build();

        userService.createNewUser(user1);
        userService.createNewUser(user2);
        userService.createNewUser(user3);

        Film film1 = Film.builder()
                .name("Test1")
                .description("TestDescription1")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        Film film2 = Film.builder()
                .name("Test2")
                .description("TestDescription2")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        Film film3 = Film.builder()
                .name("Test3")
                .description("TestDescription3")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        filmService.createNewFilm(film1);
        filmService.createNewFilm(film2);
        filmService.createNewFilm(film3);

        filmService.addLikeToFilm(film1.getId(), user1.getId());
        filmService.addLikeToFilm(film1.getId(), user2.getId());

        filmService.addLikeToFilm(film2.getId(), user1.getId());

        filmService.addLikeToFilm(film3.getId(), user1.getId());
        filmService.addLikeToFilm(film3.getId(), user2.getId());
        filmService.addLikeToFilm(film3.getId(), user3.getId());

        List<Film> topThreeFilmsByLikes = filmService.getTopFilmsByLikes(3);

        assertNotNull(topThreeFilmsByLikes);
        assertEquals(3, topThreeFilmsByLikes.size());
        assertEquals(film3.getId(), topThreeFilmsByLikes.get(0).getId());
    }
}