package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DbGenreStorageTest {
    private final Map<Integer, String> genres = Map.of(1, "Комедия", 2, "Драма", 3, "Мультфильм",
            4, "Триллер", 5, "Документальный", 6, "Боевик");

    private final GenreService genreService;

    @Test
    public void shouldGetGenreById() {
        Genre genre = genreService.getGenreById(6);

        assertNotNull(genre);
        assertTrue(genres.containsKey(genre.getId()));
        assertTrue(genres.containsValue(genre.getName()));
    }

    @Test
    public void shouldGetGenres() {
        List<Genre> genreList = genreService.getGenres();

        assertNotNull(genreList);
        assertEquals(6, genreList.size());
        genreList.forEach(genre -> assertTrue(genres.containsValue(genre.getName())));
    }
}