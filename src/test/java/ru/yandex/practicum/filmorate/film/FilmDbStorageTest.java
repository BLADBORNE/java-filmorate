package ru.yandex.practicum.filmorate.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmDbStorageTest {
    private final Map<Integer, String> ratings = Map.of(1, "G", 2, "PG");
    private final Map<Integer, String> genres = Map.of(1, "Комедия", 2, "Драма");
    private final FilmService filmService;
    private final UserService userService;
    private final DirectorService directorService;
    private final Random random = new Random();

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

        Film createdFilm = filmService.createNewFilm(film);

        Film filmFromBd = filmService.getFilmById(createdFilm.getId());

        assertNotNull(filmFromBd);
        assertEquals(1, filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(filmFromBd));
        assertEquals(createdFilm, filmFromBd);
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

        Film createdFilm = filmService.createNewFilm(film);

        Film filmFromBd = filmService.getFilmById(createdFilm.getId());

        assertNotNull(filmFromBd);
        assertEquals(1, filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(filmFromBd));
        assertEquals(createdFilm, filmFromBd);

        createdFilm.setName("UpdatedName");
        createdFilm.setGenres(List.of(new Genre(2, genres.get(2))));

        filmService.updateFilm(createdFilm);

        assertEquals(1, filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(createdFilm));
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

        Film createdFilm = filmService.createNewFilm(film);

        Film filmFromBd = filmService.getFilmById(createdFilm.getId());

        assertNotNull(filmFromBd);
        assertEquals(1, filmService.getFilms().size());
        assertTrue(filmService.getFilms().contains(filmFromBd));
        assertEquals(createdFilm, filmFromBd);

        filmService.deleteFilmById(filmFromBd.getId());

        assertEquals(0, filmService.getFilms().size());
        assertFalse(filmService.getFilms().contains(filmFromBd));
    }

    @Test
    public void addRandomScoreToFilm() {
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

        int minScore = 1;
        int maxScore = 11;
        int randomScore = random.nextInt(maxScore - minScore + 1) + minScore;

        filmService.addScoreToFilm(createdFilm.getId(), createdUser.getId(), randomScore);

        assertEquals(1, filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).size());
        assertTrue(filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).contains(createdUser));
        assertTrue(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), randomScore));
    }

    @Test
    public void shouldDeleteScore6FromFilm() {
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

        filmService.addScoreToFilm(createdFilm.getId(), createdUser.getId(), 6);

        assertEquals(1, filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).size());
        assertTrue(filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).contains(createdUser));
        assertTrue(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), 6));

        filmService.deleteScoreFromFilm(createdFilm.getId(), createdUser.getId());

        assertEquals(0, filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).size());
        assertFalse(filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).contains(createdUser));
        assertFalse(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), 6));
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

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);
        User createdUser3 = userService.createNewUser(user3);

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

        Film createdFilm1 = filmService.createNewFilm(film1);
        Film createdFilm2 = filmService.createNewFilm(film2);
        Film createdFilm3 = filmService.createNewFilm(film3);

        filmService.addScoreToFilm(createdFilm1.getId(), createdUser1.getId(), 5);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser2.getId(), 6);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser3.getId(), 7);

        filmService.addScoreToFilm(createdFilm2.getId(), createdUser1.getId(), 8);

        filmService.addScoreToFilm(createdFilm3.getId(), createdUser1.getId(), 7);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser2.getId(), 8);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser3.getId(), 10);

        List<Film> topThreeFilmsByLikes = filmService.getTopFilmsByScores(3, null, null);

        assertNotNull(topThreeFilmsByLikes);
        assertEquals(3, topThreeFilmsByLikes.size());
        assertEquals(createdFilm3.getId(), topThreeFilmsByLikes.get(0).getId());
    }

    @Test
    public void shouldGetTopFilmsByLikesAndFindOnlyWithYear1998WithCorrectSorting() {
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

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);
        User createdUser3 = userService.createNewUser(user3);

        Film film1 = Film.builder()
                .name("Test1")
                .description("TestDescription1")
                .releaseDate(LocalDate.of(1998, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1)), new Genre(2, genres.get(2))))
                .build();

        Film film2 = Film.builder()
                .name("Test2")
                .description("TestDescription2")
                .releaseDate(LocalDate.of(1998, 12, 28))
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

        Film createdFilm1 = filmService.createNewFilm(film1);
        Film createdFilm2 = filmService.createNewFilm(film2);
        Film createdFilm3 = filmService.createNewFilm(film3);

        filmService.addScoreToFilm(createdFilm1.getId(), createdUser1.getId(), 10);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser2.getId(), 9);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser3.getId(), 8);

        filmService.addScoreToFilm(createdFilm2.getId(), createdUser1.getId(), 6);

        filmService.addScoreToFilm(createdFilm3.getId(), createdUser1.getId(), 8);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser2.getId(), 8);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser3.getId(), 9);

        List<Film> topFilmsByLikes = filmService.getTopFilmsByScores(3, null, 1998);

        assertNotNull(topFilmsByLikes);
        assertEquals(2, topFilmsByLikes.size());
        assertEquals(createdFilm1.getId(), topFilmsByLikes.get(0).getId());
    }

    @Test
    public void shouldGetTopFilmsByLikesAndFindOnlyWithGenre2WithCorrectSorting() {
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

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);
        User createdUser3 = userService.createNewUser(user3);

        Film film1 = Film.builder()
                .name("Test1")
                .description("TestDescription1")
                .releaseDate(LocalDate.of(1998, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(2, genres.get(2))))
                .build();

        Film film2 = Film.builder()
                .name("Test2")
                .description("TestDescription2")
                .releaseDate(LocalDate.of(1998, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1))))
                .build();

        Film film3 = Film.builder()
                .name("Test3")
                .description("TestDescription3")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(2, genres.get(2))))
                .build();

        Film createdFilm1 = filmService.createNewFilm(film1);
        Film createdFilm2 = filmService.createNewFilm(film2);
        Film createdFilm3 = filmService.createNewFilm(film3);

        filmService.addScoreToFilm(createdFilm1.getId(), createdUser1.getId(), 1);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser2.getId(), 10);

        filmService.addScoreToFilm(createdFilm2.getId(), createdUser1.getId(), 5);

        filmService.addScoreToFilm(createdFilm3.getId(), createdUser1.getId(), 7);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser2.getId(), 9);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser3.getId(), 10);

        List<Film> topFilmsByLikes = filmService.getTopFilmsByScores(3, 2, null);

        assertNotNull(topFilmsByLikes);
        assertEquals(2, topFilmsByLikes.size());
        assertEquals(createdFilm3.getId(), topFilmsByLikes.get(0).getId());
        assertEquals(createdFilm1.getId(), topFilmsByLikes.get(1).getId());
    }

    @Test
    public void shouldGetTopFilmsByLikesAndFindOnlyWithYear1998AndCorrectGenre() {
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

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);
        User createdUser3 = userService.createNewUser(user3);

        Film film1 = Film.builder()
                .name("Test1")
                .description("TestDescription1")
                .releaseDate(LocalDate.of(1998, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(2, genres.get(2))))
                .build();

        Film film2 = Film.builder()
                .name("Test2")
                .description("TestDescription2")
                .releaseDate(LocalDate.of(1998, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(1, genres.get(1))))
                .build();

        Film film3 = Film.builder()
                .name("Test3")
                .description("TestDescription3")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(200)
                .mpa(new Rating(1, ratings.get(1)))
                .genres(List.of(new Genre(2, genres.get(2))))
                .build();

        Film createdFilm1 = filmService.createNewFilm(film1);
        Film createdFilm2 = filmService.createNewFilm(film2);
        Film createdFilm3 = filmService.createNewFilm(film3);

        filmService.addScoreToFilm(createdFilm1.getId(), createdUser1.getId(), 2);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser2.getId(), 3);

        filmService.addScoreToFilm(createdFilm2.getId(), createdUser1.getId(), 10);

        filmService.addScoreToFilm(createdFilm3.getId(), createdUser1.getId(), 4);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser2.getId(), 3);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser3.getId(), 2);

        List<Film> topFilmsByLikes = filmService.getTopFilmsByScores(3, 2, 1998);

        assertNotNull(topFilmsByLikes);
        assertEquals(1, topFilmsByLikes.size());
        assertEquals(createdFilm1.getId(), topFilmsByLikes.get(0).getId());
    }

    @Test
    public void shouldGetTopCommonsFilms() {
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

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);
        User createdUser3 = userService.createNewUser(user3);

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

        Film createdFilm1 = filmService.createNewFilm(film1);
        Film createdFilm2 = filmService.createNewFilm(film2);
        Film createdFilm3 = filmService.createNewFilm(film3);

        filmService.addScoreToFilm(createdFilm1.getId(), createdUser1.getId(), 9);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser2.getId(), 10);

        filmService.addScoreToFilm(createdFilm2.getId(), createdUser1.getId(), 6);

        filmService.addScoreToFilm(createdFilm3.getId(), createdUser1.getId(), 9);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser2.getId(), 8);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser3.getId(), 8);

        List<Film> topCommonFilms = filmService.getTopCommonFilms(createdUser1.getId(), createdUser2.getId());

        assertNotNull(topCommonFilms);
        assertEquals(2, topCommonFilms.size());
        assertEquals(createdFilm3.getId(), topCommonFilms.get(0).getId());
    }

    @Test
    public void shouldNotGetTopCommonsFilmsIfWrongIdentifiers() {
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

        User createdUser1 = userService.createNewUser(user1);
        User createdUser2 = userService.createNewUser(user2);
        User createdUser3 = userService.createNewUser(user3);

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

        Film createdFilm1 = filmService.createNewFilm(film1);
        Film createdFilm2 = filmService.createNewFilm(film2);
        Film createdFilm3 = filmService.createNewFilm(film3);

        filmService.addScoreToFilm(createdFilm1.getId(), createdUser1.getId(), 1);
        filmService.addScoreToFilm(createdFilm1.getId(), createdUser2.getId(), 2);

        filmService.addScoreToFilm(createdFilm2.getId(), createdUser1.getId(), 3);

        filmService.addScoreToFilm(createdFilm3.getId(), createdUser1.getId(), 4);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser2.getId(), 5);
        filmService.addScoreToFilm(createdFilm3.getId(), createdUser3.getId(), 6);

        assertThrows(NoSuchElementException.class, () -> {
            filmService.getTopCommonFilms(9999, 100);
        });
    }

    @Test
    public void shouldUpdateUsersScoreToRandom() {
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

        int minScore = 1;
        int maxScore = 11;
        int randomScore1 = random.nextInt(maxScore - minScore + 1) + minScore;

        filmService.addScoreToFilm(createdFilm.getId(), createdUser.getId(), randomScore1);

        assertEquals(1, filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).size());
        assertTrue(filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).contains(createdUser));
        assertTrue(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), randomScore1));

        int randomScore2 = random.nextInt(maxScore - minScore + 1) + minScore;
        filmService.addScoreToFilm(createdFilm.getId(), createdUser.getId(), randomScore2);

        assertFalse(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), randomScore1));
        assertTrue(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), randomScore2));
    }

    @Test
    public void shouldReturnFromTheMethodIfUserWantsTotUpdateTheScoreToTheCurrentScore() {
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

        int minScore = 1;
        int maxScore = 11;
        int randomScore = random.nextInt(maxScore - minScore + 1) + minScore;

        filmService.addScoreToFilm(createdFilm.getId(), createdUser.getId(), randomScore);

        assertEquals(1, filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).size());
        assertTrue(filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).contains(createdUser));
        assertTrue(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), randomScore));

        filmService.addScoreToFilm(createdFilm.getId(), createdUser.getId(), randomScore);

        assertEquals(1, filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).size());
        assertTrue(filmService.getUsersWhoScoredTheFilmById(createdFilm.getId()).contains(createdUser));
        assertTrue(filmService.getFilmScoreRecordByFilmIdUserIdAndScore(createdFilm.getId(), createdUser.getId(), randomScore));
    }
}