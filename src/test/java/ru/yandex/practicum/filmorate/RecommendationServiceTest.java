package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.recommendation.RecommedationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RecommendationServiceTest {
    private final Map<Integer, String> ratings = Map.of(1, "G", 2, "PG");
    private final Map<Integer, String> genres = Map.of(1, "Комедия", 2, "Драма");
    private final FilmService filmService;
    private final UserService userService;
    private final RecommedationService recommendationService;

    @BeforeEach
    public void createUsersAndFilms() {
        User user1 = User.builder()
                .email("belyachok567811@gmail.com")
                .login("Ilya")
                .name("BLADBORNE")
                .birthday(LocalDate.of(2024, 3, 4))
                .build();

        userService.createNewUser(user1);

        User user2 = User.builder()
                .email("feelthis@mail.ru")
                .login("RobustFoocker")
                .name("Bartasker")
                .birthday(LocalDate.of(2001, 3, 4))
                .build();

        userService.createNewUser(user2);

        User user = User.builder()
                .email("arromaticpoop@gmail.com\"")
                .login("kkkk1998")
                .name("Barabaska")
                .birthday(LocalDate.of(1998, 4, 4))
                .build();

        userService.createNewUser(user);

        Film film1 = Film.builder()
                .name("TestFilm1")
                .description("TestFilm1Description")
                .releaseDate(LocalDate.of(1899, 12, 28))
                .duration(120)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        filmService.createNewFilm(film1);

        Film film2 = Film.builder()
                .name("TestFilm2")
                .description("TestFilm2Description")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(120)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        filmService.createNewFilm(film2);

        Film film3 = Film.builder()
                .name("TestFilm1")
                .description("TestFilm1Description")
                .releaseDate(LocalDate.of(2000, 12, 28))
                .duration(130)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        filmService.createNewFilm(film3);
    }

    @Test
    public void addLikeToFilmAndAssertThatToAnotherUserWithTheSameLikeWillBeReccommendation() {
        int filmid1 = filmService.getFilms().get(0).getId();
        int filmid2 = filmService.getFilms().get(1).getId();

        int userid1 = userService.getUsers().get(0).getId();
        int userid2 = userService.getUsers().get(1).getId();

        filmService.addLikeToFilm(filmid1, userid1);
        filmService.addLikeToFilm(filmid2, userid1);
        filmService.addLikeToFilm(filmid2, userid2);

        List<Film> recommended = recommendationService.getRecommendation(userid2);

        assertEquals(filmid1, recommended.get(0).getId());
    }

    @Test
    public void recommendationWillBeEmptyIfThereIsNoLikes() {
        int filmid1 = filmService.getFilms().get(0).getId();
        int filmid2 = filmService.getFilms().get(1).getId();

        int userid1 = userService.getUsers().get(0).getId();
        int userid2 = userService.getUsers().get(1).getId();

        filmService.addLikeToFilm(filmid1, userid1);
        filmService.addLikeToFilm(filmid2, userid1);

        List<Film> recommended = recommendationService.getRecommendation(userid2);

        assertTrue(recommended.isEmpty());
    }

    @Test
    public void recommendationWillBeEmptyIfThereIsLikesIsSame() {
        int filmid1 = filmService.getFilms().get(0).getId();
        int filmid2 = filmService.getFilms().get(1).getId();

        int userid1 = userService.getUsers().get(0).getId();
        int userid2 = userService.getUsers().get(1).getId();

        filmService.addLikeToFilm(filmid1, userid1);
        filmService.addLikeToFilm(filmid2, userid1);
        filmService.addLikeToFilm(filmid1, userid2);
        filmService.addLikeToFilm(filmid2, userid2);

        List<Film> recommended = recommendationService.getRecommendation(userid2);

        assertTrue(recommended.isEmpty());
    }

    @Test
    public void recommendationWillNotFindsToIncorrectId() {
        int filmid1 = filmService.getFilms().get(0).getId();
        int filmid2 = filmService.getFilms().get(1).getId();

        int userid1 = userService.getUsers().get(0).getId();
        int userid2 = userService.getUsers().get(1).getId();

        filmService.addLikeToFilm(filmid1, userid1);
        filmService.addLikeToFilm(filmid2, userid1);
        filmService.addLikeToFilm(filmid1, userid2);
        filmService.addLikeToFilm(filmid2, userid2);


        assertThrows(NoSuchElementException.class,
                () -> recommendationService.getRecommendation(4));
    }
}
