package ru.yandex.practicum.filmorate.storage.dao.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.DateValidationException;
import ru.yandex.practicum.filmorate.exception.ScoreValidationException;
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

import static ru.yandex.practicum.filmorate.service.UserEventFactory.*;

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
    public List<Film> getTopFilmsByScores(Integer count, Integer genreId, Integer year) {
        log.info(String.format("Получен запрос на получении топ %s лучших фильмов по жанрам = %s и годам = %s",
                count,
                genreId,
                year));

        StringBuilder sql = new StringBuilder("SELECT f.*\n" +
                "FROM films AS f\n" +
                "LEFT JOIN film_score AS fs ON f.film_id = fs.film_id\n");

        List<Integer> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("JOIN film_genre AS fg ON f.film_id = fg.film_id AND fg.genre_id = ?\n");
            params.add(genreId);
        }

        if (year != null) {
            sql.append("WHERE YEAR(f.release_date) = ?\n");
            params.add(year);
        }

        sql.append("GROUP BY f.film_id\n" +
                "ORDER BY COUNT(fs.user_id) DESC, AVG(fs.score) DESC\n");

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

        String sql = "SELECT f.*\n" +
                "FROM films AS f\n" +
                "JOIN film_score fs ON f.film_id = fs.film_id\n" +
                "WHERE fs.user_id IN (?, ?)\n" +
                "GROUP BY f.film_id\n" +
                "HAVING COUNT(fs.user_id) > 1\n" +
                "ORDER BY (" +
                "SELECT COUNT(fs.user_id) " +
                "FROM film_score AS fs " +
                "WHERE f.film_id = fs.film_id" +
                ") DESC, (" +
                "SELECT AVG(fs.score) " +
                "FROM film_score AS fs " +
                "WHERE f.film_id = fs.film_id" +
                ") DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), userId1, userId2);
    }

    @Override
    public void addScoreToFilm(int filmId, int userId, int score) {
        log.info(String.format("Получен запрос на добавление оценки фильму с id = %s от пользователя  c id = %s",
                filmId, userId));

        checkScoreValidation(userId, score);

        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        SqlRowSet currentFilmScore = jdbcTemplate.queryForRowSet("SELECT * FROM film_score AS fs WHERE " +
                "fs.film_id = ? AND fs.user_id = ? ", filmId, userId);

        if (currentFilmScore.next()) {
            int curScore = currentFilmScore.getInt("score");

            if (curScore != score) {
                log.info("Пользователь {} успешно изменил оценку {} на {} фильму {}", user.getName(), curScore, score,
                        film.getName());

                jdbcTemplate.update("UPDATE film_score SET score = ? WHERE film_id = ? AND user_id = ?", score,
                        filmId, userId);

                userStorage.registerUserEvent(getUpdateFilmScoreEvent(userId, filmId));

                return;
            }

            log.warn("Предупреждение: пользователь {} пытался поставить ту же оценку фильму {}", user.getName(),
                    film.getName());

            throw new AlreadyExistException(String.format("У вас уже стоит текущая оценка: %d, выберите другую для " +
                    "успешной замены", score));
        }

        jdbcTemplate.update("INSERT INTO film_score (film_id, user_id, score) VALUES (?, ?, ?)", filmId, userId, score);

        log.info(String.format("Пользователь %s успешно поставил оценку %d фильму %s", user.getName(), score,
                film.getName()));

        userStorage.registerUserEvent(getAddFilmScoreEvent(userId, filmId));
    }

    @Override
    public void deleteScoreFromFilm(int filmId, int userId) {
        log.info(String.format("Получен запрос на удаление оценки фильму с id = %s от пользователя c id = %s", filmId,
                userId));

        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        jdbcTemplate.update("DELETE FROM film_score WHERE film_id = ? AND user_id = ?", filmId, userId);

        log.info(String.format("Пользователь %s успешно удалил оценку фильму %s", user.getName(), film.getName()));

        userStorage.registerUserEvent(getDeleteFilmScoreEvent(userId, filmId));
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
                sql = "SELECT f.*" +
                        "FROM FILMS f " +
                        "LEFT JOIN FILM_DIRECTOR fd ON f.FILM_ID = fd.FILM_ID " +
                        "LEFT JOIN DIRECTOR d ON d.ID = fd.DIRECTOR_ID " +
                        "LEFT JOIN film_score AS fs ON fs.film_id = f.film_id " +
                        "WHERE d.id = ? " +
                        "GROUP BY f.film_id " +
                        "ORDER BY COUNT(fs.user_id) DESC, AVG(fs.score) DESC";
                break;
            default:
                throw new ValidationException("Неизвестный параметр сортировки sortBy=" + sortBy);
        }

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), directorId);
    }

    @Override
    public List<User> getUsersWhoScoredTheFilmById(int id) {
        log.info("Получен запрос на отправление всех людей, котрые оценили фильм с id = {}", id);

        getFilmById(id);

        String sql = "SELECT u.*\n" +
                "FROM users AS u\n" +
                "JOIN film_score AS fs ON u.user_id = fs.user_id\n" +
                "WHERE fs.film_id = ?";

        log.info("Фильму с id = {} успешно отправлен список людей, оценивших фильм", id);

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeUser(rs), id);
    }

    @Override
    public boolean getFilmScoreRecordByFilmIdUserIdAndScore(int filmId, int userId, int score) {
        log.info("Получен запрос на отправку оценки фильма с id = {} от пользователя с id = {} и оценкой = {}", filmId,
                userId, score);

        getFilmById(filmId);
        getFilmById(userId);

        SqlRowSet scoreRows = jdbcTemplate.queryForRowSet("SELECT fs.* FROM film_score AS fs WHERE fs.film_id = ? " +
                "AND fs.user_id = ? AND fs.score = ?", filmId, userId, score);

        if (scoreRows.next()) {
            log.info("Запись успешно найдена");

            return true;
        }

        log.info("Запись не найдена");

        return false;
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        log.info("Получен запрос = {} на поиск с фильтром по = {}", query, by);

        String dbQuery = "%" + query + "%";

        switch (by) {
            case "title":
                String sqlTitle = "SELECT f.* " +
                        "FROM films AS f " +
                        "LEFT JOIN film_score AS fs ON f.film_id = fs.film_id " +
                        "WHERE LOWER(f.name) LIKE LOWER(?) " +
                        "GROUP BY f.film_id " +
                        "ORDER BY COUNT(fs.user_id) DESC, AVG(fs.score) DESC";

                return jdbcTemplate.query(sqlTitle, (rs, rowNum) -> makeFilm(rs), dbQuery);
            case "director":
                String sqlDirector = "SELECT f.* " +
                        "FROM films AS f " +
                        "JOIN film_director AS fd ON f.film_id = fd.film_id " +
                        "JOIN director AS d ON fd.director_id = d.id " +
                        "LEFT JOIN film_score AS fs ON f.film_id = fs.film_id " +
                        "WHERE LOWER(d.name) LIKE LOWER(?) " +
                        "GROUP BY f.film_id " +
                        "ORDER BY COUNT(fs.user_id) DESC, AVG(fs.score) DESC";

                return jdbcTemplate.query(sqlDirector, (rs, rowNum) -> makeFilm(rs), dbQuery);
            case "director,title":
            case "title,director":
                String sqlDirectorOrTitle = "SELECT f.* " +
                        "FROM films AS f " +
                        "LEFT JOIN film_director AS fd ON f.film_id = fd.film_id " +
                        "LEFT JOIN director AS d ON fd.director_id = d.id " +
                        "LEFT JOIN film_score AS fs ON f.film_id = fs.film_id " +
                        "WHERE LOWER(d.name) LIKE LOWER(?) " +
                        "OR LOWER(f.name) LIKE LOWER(?) " +
                        "GROUP BY f.film_id " +
                        "ORDER BY COUNT(fs.user_id) DESC, AVG(fs.score) DESC";

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

    private void checkScoreValidation(int userId, int score) {
        if (score <= 0 || score >= 11) {
            log.warn("Клиент с id = {} передал неправильную оценку: - {}", userId, score);

            throw new ScoreValidationException(String.format("Оценка должна быть в диапозоне: [1; 10], ваша оценка - " +
                    "%d", score));
        }
    }
}