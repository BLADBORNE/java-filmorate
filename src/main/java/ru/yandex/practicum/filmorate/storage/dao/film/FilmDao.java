package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.dao.film.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.dao.film.rating.RatingStorage;
import ru.yandex.practicum.filmorate.storage.dao.user.UserStorage;

import javax.validation.ValidationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static ru.yandex.practicum.filmorate.service.UserEventFactory.getAddLike;
import static ru.yandex.practicum.filmorate.service.UserEventFactory.getDeleteLike;

@Component
@Slf4j
@RequiredArgsConstructor
public class FilmDao implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RatingStorage ratingStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

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
                genreStorage.getFilmsGenres(rs.getInt("film_id")),
                directorStorage.getFilmsDirectors(rs.getInt("film_id")));
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
                    genreStorage.getFilmsGenres(filmRows.getInt("film_id")),
                    directorStorage.getFilmsDirectors(filmRows.getInt("film_id")));
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

        log.info("Фильм {} успешно создан", film.getName());

        film.setId(generatedId.intValue());
        genreStorage.updateFilmGenres(film);
        directorStorage.updateFilmDirectors(film);

        return getFilmById(generatedId.intValue());
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
        directorStorage.updateFilmDirectors(film);

        return getFilmById(film.getId());
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
                "GROUP BY f.film_id\n" +
                "ORDER BY COUNT(fl.user_id) DESC\n" +
                "LIMIT ?;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    @Override
    public List<Film> getTopFilmsByLikes(Integer count, Integer genreId, Integer year) {
        log.info(String.format("Получен запрос на получении топ %s лучших фильмов по жанрам = %s и годам = %s",
                count,
                genreId,
                year));

        StringBuilder sql = new StringBuilder("SELECT f.*\n" +
                "FROM films AS f\n" +
                "LEFT JOIN film_like AS fl ON f.film_id = fl.film_id\n");

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genre AS fg ON f.film_id = fg.film_id AND fg.genre_id = ?\n");
            params.add(genreId);
        }

        if (year != null) {
            sql.append("WHERE YEAR(f.release_date) = ?\n");
            params.add(year);
        }

        sql.append("GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id\n" +
                "ORDER BY COUNT(fl.user_id) DESC\n");

        if (count != null) {
            sql.append("LIMIT ?;");
            params.add(count);
        }


        log.info(String.format("Топ %s лучших фильмов по жанрам = %s и годам = %s отправлены клиенту",
                count,
                genreId,
                year));

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public List<Film> getTopCommonFilms(int userId1, int userId2) {

        log.info(String.format("Получен запрос на получение общих фильмов для пользователей %s и %s",
                userId1, userId2));

        userStorage.getUserById(userId1);
        userStorage.getUserById(userId2);

        log.info(String.format("Общие фильмы для пользователей %s и %s отправлены клиенту", userId1, userId2));

        String sql = "SELECT f.* \n" +
                "FROM films f \n" +
                "WHERE f.film_id IN (\n" +
                "    SELECT film_id \n" +
                "    FROM film_like\n" +
                "    WHERE user_id IN (?, ?) \n" +
                "    GROUP BY film_id \n" +
                "    HAVING COUNT(film_id) > 1\n" +
                ") \n" +
                "ORDER BY (\n" +
                "    SELECT COUNT(*) \n" +
                "    FROM film_like \n" +
                "    WHERE f.film_id = film_id\n" +
                ") DESC;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), userId1, userId2);
    }

    @Override
    public void addLikeToFilm(int filmId, int userId) {
        log.info(String.format("Получен запрос на добавление лайка фильму с id = %s от пользователя  c id = %s",
                filmId, userId));

        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        jdbcTemplate.update("INSERT INTO film_like (film_id, user_id) VALUES (?, ?)", filmId, userId);

        log.info(String.format("Пользователь %s успешно поставил лайк фильму %s", user.getName(), film.getName()));
        userStorage.registerUserEvent(getAddLike(userId, filmId));
    }

    @Override
    public void deleteLikeFromFilm(int filmId, int userId) {
        log.info(String.format("Получен запрос на удаление лайка фильму с id = %s от пользователя c id = %s", filmId,
                userId));

        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        jdbcTemplate.update("DELETE FROM film_like WHERE film_id = ? AND user_id = ?", filmId, userId);

        log.info(String.format("Пользователь %s успешно удалил лайк фильму %s", user.getName(), film.getName()));
        userStorage.registerUserEvent(getDeleteLike(userId, filmId));
    }

    @Override
    public List<Film> getDirectorFilm(int directorId, String sortBy) {
        directorStorage.getDirectorById(directorId);
        String sql;
        switch (sortBy) {
            case "year":
                sql = "SELECT f.* FROM FILMS f " +
                        "LEFT JOIN FILM_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID " +
                        "LEFT JOIN DIRECTOR d ON d.ID = fd.DIRECTOR_ID " +
                        "WHERE d.id = ? " +
                        "ORDER BY f.release_date";
                break;
            case "likes":
                sql = "SELECT f.*, COUNT(fl.*) as likes FROM FILMS f " +
                        "LEFT JOIN FILM_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID " +
                        "LEFT JOIN DIRECTOR d ON d.ID = fd.DIRECTOR_ID " +
                        "LEFT JOIN film_like AS fl ON fl.film_id = f.film_id " +
                        "WHERE d.id = ? " +
                        "GROUP BY f.film_id " +
                        "ORDER BY likes DESC";
                break;
            default:
                throw new ValidationException("Неизвестный параметр сортировки sortBy=" + sortBy);
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), directorId);
    }

    @Override
    public List<User> getFilmLikes(int id) {
        log.info("Получен запрос на отправление всех лайков от людей фильму с id = {}", id);

        getFilmById(id);

        String sql = "\n" +
                "SELECT u.*\n" +
                "FROM users AS u\n" +
                "WHERE u.user_id IN\n" +
                "    (SELECT user_id\n" +
                "     FROM film_like AS fl\n" +
                "     WHERE fl.film_id = ?)";

        log.info("Фильму с id = {} успешно отправлен всех лайков от людей", id);

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id);
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        log.info("Получен запрос = {} на поиск с фильтром по = {}", query, by);
        String dbQuery = "%" + query + "%";

        switch (by) {
            case "title":
                String sqlTitle =
                        "SELECT f.* " +
                        "FROM films AS f " +
                        "LEFT JOIN film_like AS l ON f.film_id = l.film_id " +
                        "WHERE LOWER(f.name) LIKE LOWER(?) " +
                        "GROUP BY f.film_id " +
                        "ORDER BY COUNT(l.user_id) DESC;";
                return jdbcTemplate.query(sqlTitle, (rs, rowNum) -> makeFilm(rs), dbQuery);
            case "director":
                String sqlDirector =
                        "SELECT f.* " +
                        "FROM films AS f " +
                        "JOIN film_director AS fd ON f.film_id = fd.film_id " +
                        "JOIN director AS d ON fd.director_id = d.id " +
                        "LEFT JOIN film_like AS l ON f.film_id = l.film_id " +
                        "WHERE LOWER(d.name) LIKE LOWER(?) " +
                        "GROUP BY f.film_id " +
                        "ORDER BY COUNT(l.user_id) DESC;";
                return jdbcTemplate.query(sqlDirector, (rs, rowNum) -> makeFilm(rs), dbQuery);
            case "director,title":
            case "title,director":
                String sqlDirectorOrTitle =
                        "SELECT f.* " +
                        "FROM films AS f " +
                        "LEFT JOIN film_director AS fd ON f.film_id = fd.film_id " +
                        "LEFT JOIN director AS d ON fd.director_id = d.id " +
                        "LEFT JOIN film_like AS l ON f.film_id = l.film_id " +
                        "WHERE LOWER(d.name) LIKE LOWER(?) " +
                                "OR LOWER(f.name) LIKE LOWER(?) " +
                        "GROUP BY f.film_id " +
                        "ORDER BY COUNT(l.user_id) DESC;";
                return jdbcTemplate.query(sqlDirectorOrTitle, (rs, rowNum) -> makeFilm(rs), dbQuery, dbQuery);
            default:
                String errorMessage = String.format("Параметр сортрировки {} для поиска не найден", by);
                log.error(errorMessage);
                throw new NoSuchElementException(errorMessage);
        }
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

            throw new DateValidationException("Дата фильма должна быть не меньше 1895-12-28");
        }
    }
}