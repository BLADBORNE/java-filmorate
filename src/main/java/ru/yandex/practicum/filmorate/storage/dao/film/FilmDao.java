package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.dao.rating.RatingStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@Slf4j
public class FilmDao implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RatingStorage ratingStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmDao(JdbcTemplate jdbcTemplate, RatingStorage ratingStorage, UserStorage userStorage, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.ratingStorage = ratingStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public List<Film> getFilms() {
        log.info("Получен запрос на отправку всех фильмов");

        String sql = "SELECT * FROM films";

        log.info("Все фильмы были успешно отправлены!");

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        return new Film(
                rs.getInt("film_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                ratingStorage.getRatingById(rs.getInt("rating_id")),
                genreStorage.getFilmsGenres(rs.getInt("film_id")));
    }

    @Override
    public Film getFilmById(int id) {
        log.info(String.format("Получен запрос на отправку фильма с id = %s", id));

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE film_id = ?", id);

        if (filmRows.next()) {
            log.info(String.format("Фильм с id = %s успешно отправлен клиенту", id));

            return new Film(
                    filmRows.getInt("film_id"),
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"),
                    ratingStorage.getRatingById(filmRows.getInt("rating_id")),
                    genreStorage.getFilmsGenres(filmRows.getInt("film_id")));
        }

        log.warn(String.format("Отсутствует фильм с id = %s", id));

        throw new NoSuchElementException(String.format("Фильм с id = %s отсутствует", id));
    }

    @Override
    public Film createNewFilm(Film film) {
        log.info("Получен запрос на создание нового фильма");

        checkDateValidation(film.getReleaseDate());
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("rating_id", film.getMpa().getId());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        film.setId(generatedId.intValue());

        log.info("Фильм {} успешно создан", film.getName());

        genreStorage.updateFilmGenres(film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Получен запрос на обновление фильма");

        checkDateValidation(film.getReleaseDate());
        getFilmById(film.getId());

        jdbcTemplate.update("UPDATE films SET name = ?, description = ?, release_date = ?," +
                        "duration = ?, rating_id = ? WHERE film_id = ?", film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());

        log.info("Фильм с id = {} успешно обновлен", film.getId());

        genreStorage.updateFilmGenres(film);

        return film;
    }

    @Override
    public Film deleteFilmById(int id) {
        log.info(String.format("Получен запрос на удаление фильма с id = %s", id));

        Film deletedFilm = getFilmById(id);

        jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", id);

        log.info("Фильм {} был успешно удален", deletedFilm.getName());

        return deletedFilm;
    }

    @Override
    public List<Film> getTopFilmsByLikes(int count) {
        log.info(String.format("Получен запрос на получении топ %s лучших фильмов", count));

        log.info(String.format("Топ %s лучших фильмов отправлены клиенту", count));

        String sql = "SELECT f.*\n" +
                "FROM films AS f\n" +
                "LEFT JOIN film_like AS fl ON f.film_id = fl.film_id\n" +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id\n" +
                "ORDER BY COUNT(fl.user_id) DESC\n" +
                "LIMIT ?;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    @Override
    public void addLikeToFilm(int filmId, int userId) {
        log.info(String.format("Получен запрос на добавление лайка фильму с id = %s от пользователя  c id = %s",
                filmId, userId));

        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        jdbcTemplate.update("INSERT INTO film_like (film_id, user_id) VALUES (?, ?)", filmId, userId);

        log.info(String.format("Пользователь %s успешно поставил лайк фильму %s", user.getName(), film.getName()));
    }

    @Override
    public void deleteLikeFromFilm(int filmId, int userId) {
        log.info(String.format("Получен запрос на удаление лайка фильму с id = %s от пользователя c id = %s", filmId,
                userId));

        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        jdbcTemplate.update("DELETE FROM film_like WHERE film_id = ? AND user_id = ?", filmId, userId);

        log.info(String.format("Пользователь %s успешно удалил лайк фильму %s", user.getName(), film.getName()));
    }

    @Override
    public List<User> getFilmLikes(int id) {
        log.info("Получен запрос на отправление всех лайков от людей фильму с id = {}", id);

        getFilmById(id);

        String sql = "\n" +
                "SELECT *\n" +
                "FROM users AS u\n" +
                "WHERE u.user_id IN\n" +
                "    (SELECT user_id\n" +
                "     FROM film_like AS fl\n" +
                "     WHERE fl.film_id = ?)";

        log.info("Фильму с id = {} успешно отправлен всех лайков от людей", id);

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id);
    }

    private User makeUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate());
    }

    private void checkDateValidation(LocalDate date) {
        if (date.isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("При создании фильма поле дата-релиза объекта Film не прошло валидацию");

            throw new DateValidationException("Дата фильма должна быть не менше 1895-12-28");
        }
    }
}