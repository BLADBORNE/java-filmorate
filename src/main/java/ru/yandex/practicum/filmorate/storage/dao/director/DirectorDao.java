package ru.yandex.practicum.filmorate.storage.dao.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectorDao implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getDirectors() {
        log.info("Получен запрос на отправку всех режиссеров");
        String sql = "SELECT * FROM director";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeDirector(rs)).stream().sorted(Comparator.comparing(Director::getId))
                .collect(Collectors.toList());
    }

    @Override
    public Director getDirectorById(int id) {
        log.info("Получен запрос на отправку режиссера с id = {}", id);

        SqlRowSet directorRows = jdbcTemplate.queryForRowSet("SELECT * FROM director WHERE id = ?", id);

        if (directorRows.next()) {
            return new Director(
                    directorRows.getInt("id"),
                    directorRows.getString("name"));
        }

        throw new NoSuchElementException(String.format("Режиссер с id = %s отсутствует", id));
    }

    @Override
    public List<Director> getFilmsDirectors(int id) {
        log.info("Получен запрос на отправку всех режиссеров фильму с id = {}", id);

        String sql = "\n" +
                "SELECT *\n" +
                "FROM director d\n" +
                "JOIN film_director fd ON d.id = fd.director_id\n" +
                "WHERE fd.film_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeDirector(rs), id).stream().sorted(Comparator
                .comparing(Director::getId)).collect(Collectors.toList());
    }

    @Override
    public Director createNewDirector(Director director) {
        log.info("Получен запрос на создание нового режиссера");

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("director")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", director.getName());

        Number generatedId = jdbcInsert.executeAndReturnKey(parameters);

        director.setId(generatedId.intValue());

        log.info("Режиссер {} успешно создан", director.getName());
        return director;
    }

    @Override
    public Director updateNewDirector(Director director) {
        log.info("Получен запрос на обновление режиссера");

        getDirectorById(director.getId());

        jdbcTemplate.update("UPDATE director SET name = ? WHERE id = ?", director.getName(), director.getId());

        log.info("Режисссер с id = {} успешно обновлен", director.getId());

        return director;
    }

    @Override
    public void updateFilmDirectors(Film film) {
        log.info("Получен запрос на добавление режиссеров фильму");

        if (film.getDirectors() == null) {
            deleteDirectorsByFilmId(film.getId());
            return;
        }

        List<Integer> currentFilmDirectors = getFilmsDirectors(film.getId()).stream().map(Director::getId).collect(Collectors.toList());
        Set<Integer> uniqueUpdatedFilmDirectors = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());

        List<Integer> removedFilmDirectors = new ArrayList<>(currentFilmDirectors);
        List<Integer> addedFilmDirectors = new ArrayList<>(uniqueUpdatedFilmDirectors);

        removedFilmDirectors.removeAll(uniqueUpdatedFilmDirectors);
        addedFilmDirectors.removeAll(currentFilmDirectors);

        if (!removedFilmDirectors.isEmpty()) {
            deleteFilmDirectors(film, removedFilmDirectors);
        }

        if (!addedFilmDirectors.isEmpty()) {
            addDirectorsToFilm(film, addedFilmDirectors);
        }
    }

    public void deleteFilmDirectors(Film film, List<Integer> removedFilmDirectors) {
        log.info(String.format("Получен запрос на удаление режиссера фильма %s", film.getName()));

        removedFilmDirectors.forEach(id -> {
            jdbcTemplate.update("DELETE FROM film_director WHERE id = ? ", id);

            log.info("Режиссер {} успешно удален у фильма {}", id, film.getName());
        });
    }

    public void deleteFilmDirectors(int directorId) {
        log.info(String.format("Получен запрос на удаление режиссера %s у всех фильмов", directorId));
        jdbcTemplate.update("DELETE FROM film_director WHERE director_id = ? ", directorId);
        log.info("Режиссер {} успешно удален у всех фильмов", directorId);
    }

    public void deleteDirectorsByFilmId(int film_id) {
        log.info(String.format("Получен запрос на удаление режиссеров у фильма $s", film_id));
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ? ", film_id);
        log.info("Режиссеры успешно удалены у фильма {}", film_id);
    }

    @Override
    public Director deleteDirector(int id) {
        log.info(String.format("Получен запрос на удаление режиссера %s", id));
        Director director = getDirectorById(id);
        deleteFilmDirectors(id);
        jdbcTemplate.update("DELETE FROM director WHERE id = ?", id);
        return director;
    }

    public void addDirectorsToFilm(Film film, List<Integer> addedFilmDirectors) {
        addedFilmDirectors.forEach(id -> {
            jdbcTemplate.update("INSERT INTO film_director (film_id, director_id) " + "VALUES (?, ?)",
                    film.getId(), id);

            log.info("Режиссер {} успешно добавлен фильму {}", id, film.getName());
        });
    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        return new Director(
                rs.getInt("id"),
                rs.getString("name"));
    }
}
