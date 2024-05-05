package ru.yandex.practicum.filmorate.storage.dao.film.review;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@AllArgsConstructor
@Slf4j
public class ReviewDao implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Override
    public Review createNewReview(Review review) {
        log.info("Получен запрос на создание нового отзыва фильму c id = {} от пользователя c id = {}",
                review.getFilmId(), review.getUserId());

        Film film = filmStorage.getFilmById(review.getFilmId());
        User user = userStorage.getUserById(review.getUserId());

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("film_id", review.getFilmId());
        parameters.put("user_id", review.getUserId());
        parameters.put("is_positive", review.getIsPositive());
        parameters.put("content", review.getContent());
        parameters.put("useful", 0);

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        log.info("Успешно создан отзыв фильму {} от пользователя {}", film.getName(), user.getName());

        return getReviewById(generatedId.intValue());
    }

    @Override
    public Review updateReview(Review review) {
        log.info("Получен запрос на обновление отзыва фильму с id = {} от пользователя c id = {}",
                review.getFilmId(), review.getUserId());

        getReviewById(review.getReviewId());

        jdbcTemplate.update("UPDATE reviews SET is_positive = ?, content = ? WHERE review_id = ?",
                review.getIsPositive(), review.getContent(), review.getReviewId());

        log.info("Успешно обновлен отзыв у фильма {} от пользователя {}", filmStorage.getFilmById(review.getFilmId())
                .getName(), userStorage.getUserById(review.getUserId()).getName());

        return getReviewById(review.getReviewId());
    }

    @Override
    public void deleteReviewById(int id) {
        log.info("Получен запрос на удаление отзыва с id = {}", id);

        Review review = getReviewById(id);

        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id);

        log.info("Успешно удален отзыв у фильма {} от пользовател {}", filmStorage.getFilmById(review.getFilmId()),
                userStorage.getUserById(review.getUserId()));

    }

    @Override
    public Review getReviewById(int id) {
        log.info("Полчуен запрос на получения отзыва с id = {}", id);

        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT * FROM reviews WHERE review_id = ?", id);

        if (reviewRows.next()) {
            log.info("Отзыв с id = {} успешно отправлен клиенту", id);

            return new Review(
                    reviewRows.getInt("review_id"),
                    reviewRows.getString("content"),
                    reviewRows.getBoolean("is_positive"),
                    reviewRows.getInt("user_id"),
                    reviewRows.getInt("film_id"),
                    reviewRows.getInt("useful"));
        }

        log.warn("Отсутствует отзыв с id = {}", id);

        throw new NoSuchElementException(String.format("Отзыв с id = %s отсутствует", id));
    }

    @Override
    public List<Review> getFilmsReviews(int id, int count) {
        log.info("Получен запрос на отправку всех отзывов фильму с id = {}", id);

        int limit = count >= 1 ? count : 10;
        StringBuilder sqlBuilder = new StringBuilder();

        sqlBuilder.append("SELECT r.*\n")
                .append("FROM reviews AS r\n");

        Film film = null;

        if (id != 0) {
            film = filmStorage.getFilmById(id);

            sqlBuilder.append("WHERE r.film_id = ?\n");
        }

        sqlBuilder.append("ORDER BY r.useful DESC\n")
                .append("LIMIT ?");

        String sql = sqlBuilder.toString();

        if (id != 0) {
            log.info("Фильму {} успешно отправлено {} отзывов", film.getName(), limit);

            return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), id, limit);
        }

        log.info("Успешно отправлены все отзывы, тк не был передан корректный id фильма");

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), limit);
    }

    @Override
    public void addLikeToReview(int reviewId, int userId) {
        log.info("Получен запрос на добавлене лайка отзыву с id = {} от пользователя с id = {}", reviewId, userId);

        getReviewById(reviewId);
        User user = userStorage.getUserById(userId);

        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT * FROM review_like WHERE review_id = ? AND " +
                "user_id = ?", reviewId, userId);

        if (reviewRows.next()) {
            if (!reviewRows.getBoolean("is_positive")) {
                jdbcTemplate.update("UPDATE review_like SET is_positive = true WHERE review_id = ? AND user_id = ?",
                        reviewId, userId);

                log.info("Успешно обновлен дизлайк на лайк у отзыва с id = {} от пользователя {}", reviewId, user.getName());

                jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", reviewId);

                log.info("Успешно увиличилась полезность на 1 единицу у отзыва с id = {}", reviewId);

                return;
            }

            log.warn("Предупреждение: Пользователь {} пытается проставить повторно лайк отзыву с id = {}",
                    user.getName(), reviewId);

            throw new AlreadyExistException(String.format("Пользователь не может повторно ставить лайк отзыву, " +
                    "id клиента = %d", userId));
        }

        jdbcTemplate.update("INSERT INTO review_like (review_id, user_id, is_positive) VALUES (?, ?, true)",
                reviewId, userId);

        log.info("Успешно поставлен лайк отзыву с id = {} от пользователя {}", reviewId, user.getName());

        jdbcTemplate.update("UPDATE reviews SET useful = useful + 1  WHERE review_id = ?", reviewId);

        log.info("Успешно увиличилась полезность на 1 единицу у отзыва с id = {}", reviewId);
    }

    @Override
    public void addDislikeToReview(int reviewId, int userId) {
        log.info("Получен запрос на добавлене дизлайка отзыву с id = {} от пользователя с id = {}", reviewId, userId);

        getReviewById(reviewId);
        User user = userStorage.getUserById(userId);

        SqlRowSet reviewRows = jdbcTemplate.queryForRowSet("SELECT * FROM review_like WHERE review_id = ? AND " +
                "user_id = ?", reviewId, userId);

        if (reviewRows.next()) {
            if (reviewRows.getBoolean("is_positive")) {
                jdbcTemplate.update("UPDATE review_like SET is_positive = false WHERE review_id = ? AND user_id = ?",
                        reviewId, userId);

                log.info("Успешно обновлен лайк на дизлайк у отзыва с id = {} от пользователя {}", reviewId, user.getName());

                jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);

                log.info("Успешно уменьшилась полезность на 1 единицу у отзыва с id = {}", reviewId);

                return;
            }

            log.warn("Предупреждение: Пользователь {} пытается проставить повторно дизлайк отзыву с id = {}",
                    user.getName(), reviewId);

            throw new AlreadyExistException(String.format("Пользователь не может повторно ставить дизлайк отзыву, " +
                    "id клиента = %d", userId));
        }

        jdbcTemplate.update("INSERT INTO review_like (review_id, user_id, is_positive) VALUES (?, ?, false)",
                reviewId, userId);

        log.info("Успешно поставлен дизлайк отзыву с id = {} от пользователя {}", reviewId, user.getName());

        jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);

        log.info("Успешно уменьшилась полезность на 1 единицу у отзыва с id = {}", reviewId);
    }

    @Override
    public void deleteLikeFromReview(int reviewId, int userId) {
        log.info("Получен запрос на удаление лайка у отзыва с id = {} от пользователя с id = {}", reviewId, userId);

        getReviewById(reviewId);
        User user = userStorage.getUserById(userId);

        jdbcTemplate.update("DELETE FROM review_like WHERE review_id = ? AND user_id = ? AND is_positive = true",
                reviewId, userId);

        log.info("Успешно удален лайк у отзыва с id = {} от пользователя {}", reviewId, user.getName());

        jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE review_id = ?", reviewId);

        log.info("Успешно уменьшилась полезность на 1 единицу у отзыва с id = {}", reviewId);
    }

    @Override
    public void deleteDislikeFromReview(int reviewId, int userId) {
        log.info("Получен запрос на удаление дизлайка у отзыва с id = {} от пользователя с id = {}", reviewId, userId);

        getReviewById(reviewId);
        User user = userStorage.getUserById(userId);

        jdbcTemplate.update("DELETE FROM review_like WHERE review_id = ? AND user_id = ? AND is_positive = false",
                reviewId, userId);

        log.info("Успешно удален дизлайк у отзыва с id = {} от пользователя {}", reviewId, user.getName());

        jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE review_id = ?", reviewId);

        log.info("Успешно увеличилась полезность на 1 единицу у отзыва с id = {}", reviewId);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        return new Review(
                rs.getInt("review_id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("useful"));
    }
}