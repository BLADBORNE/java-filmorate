package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RatingDbStorageTest {
    private final Map<Integer, String> ratings = Map.of(1, "G", 2, "PG", 3, "PG-13", 4,
            "R", 5, "NC-17");

    private final RatingService ratingService;

    @Test
    public void shouldGetRatingById() {
        Rating rating = ratingService.getRatingById(4);

        assertNotNull(rating);
        assertTrue(ratings.containsKey(rating.getId()));
        assertTrue(ratings.containsValue(rating.getName()));
    }

    @Test
    public void shouldGetRatings() {
        List<Rating> ratingList = ratingService.getRatings();

        assertNotNull(ratingList);
        assertEquals(5, ratingList.size());
        ratingList.forEach(rating -> assertTrue(ratings.containsValue(rating.getName())));
    }
}