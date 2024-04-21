package ru.yandex.practicum.filmorate.storage.dao.rating;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RatingDao implements RatingStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RatingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Rating getRatingById(int id) {
        log.info("Получен запрос на отправку рейтинга с id = {}", id);

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM ratings WHERE rating_id = ?", id);

        if (filmRows.next()) {
            log.info("Рейтинг с id = {} успешно отправлен клиенту", id);

            return new Rating(
                    filmRows.getInt("rating_id"),
                    filmRows.getString("rating"));
        }

        throw new NoSuchElementException(String.format("Рейтинг с id = %s отсутствует", id));
    }

    @Override
    public List<Rating> getRatings() {
        log.info("Получен запрос на отправку всех рейтингов");

        String sql = "SELECT * FROM ratings";

        log.info("Все рейтинги были успешно отправлены клиенту!");

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeRating(rs)).stream().sorted(Comparator
                .comparing(Rating::getId)).collect(Collectors.toList());
    }

    private Rating makeRating(ResultSet rs) throws SQLException {
        return new Rating(
                rs.getInt("rating_id"),
                rs.getString("rating"));
    }
}