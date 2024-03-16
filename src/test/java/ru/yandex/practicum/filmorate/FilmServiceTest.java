package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmServiceTest {
    private FilmService service;
    private InMemoryUserStorage userStorage;

    @BeforeEach
    public void createFilmService() {
        userStorage = new InMemoryUserStorage();
        service = new FilmService(new InMemoryFilmStorage(), userStorage);
    }

    @Test
    public void addLikeToFilm() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userStorage.createNewUser(user);

        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        service.createNewFilm(film);

        service.addLikeToFilm(user.getId(), film.getId());

        assertEquals(1, film.getFilmLikes().size());
        assertTrue(film.getFilmLikes().contains(user.getId()));
    }

    @Test
    public void shouldDeleteLikeFromFilm() {
        User user = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userStorage.createNewUser(user);

        Film film = Film.builder()
                .name("Test")
                .description("TestDescription")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        service.createNewFilm(film);

        service.addLikeToFilm(user.getId(), film.getId());

        assertEquals(1, film.getFilmLikes().size());
        assertTrue(film.getFilmLikes().contains(user.getId()));

        service.deleteLikeFromFilm(film.getId(), user.getId());

        assertEquals(0, film.getFilmLikes().size());
        assertFalse(film.getFilmLikes().contains(user.getId()));
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

        userStorage.createNewUser(user1);
        userStorage.createNewUser(user2);
        userStorage.createNewUser(user3);

        Film film1 = Film.builder()
                .name("Test1")
                .description("TestDescription1")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        Film film2 = Film.builder()
                .name("Test2")
                .description("TestDescription2")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        Film film3 = Film.builder()
                .name("Test3")
                .description("TestDescription3")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .build();

        service.createNewFilm(film1);
        service.createNewFilm(film2);
        service.createNewFilm(film3);

        service.addLikeToFilm(film1.getId(), user1.getId());
        service.addLikeToFilm(film1.getId(), user2.getId());

        service.addLikeToFilm(film2.getId(), user1.getId());

        service.addLikeToFilm(film3.getId(),user1.getId());
        service.addLikeToFilm(film3.getId(),user2.getId());
        service.addLikeToFilm(film3.getId(),user3.getId());

        List<Film> topThreeFilmsByLikes = service.getTopFilmsByLikes(3);

        assertNotNull(topThreeFilmsByLikes);
        assertEquals(3, topThreeFilmsByLikes.size());
        assertEquals(film3, topThreeFilmsByLikes.get(0));
    }
}