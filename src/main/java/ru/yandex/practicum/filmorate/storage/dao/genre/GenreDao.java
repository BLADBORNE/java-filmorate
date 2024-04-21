package ru.yandex.practicum.filmorate.storage.dao.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GenreDao implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenreById(int id) {
        log.info("Получен запрос на отправку жанра с id = {}", id);

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE genre_id = ?", id);

        if (filmRows.next()) {
            log.info("Жанр с id = {} успешно отправлен клиенту", id);

            return new Genre(
                    filmRows.getInt("genre_id"),
                    filmRows.getString("genre"));
        }

        throw new NoSuchElementException(String.format("Рейтинг с id = %s отсутствует", id));
    }

    @Override
    public List<Genre> getGenres() {
        log.info("Получен запрос на отправку всех жанров");

        String sql = "SELECT * FROM genres";

        log.info("Все жанры были успешно отправлены клиенту!");

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs)).stream().sorted(Comparator.comparing(Genre::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Genre> getFilmsGenres(int id) {
        log.info("Получен запрос на отправку всех жанров фильму с id = {}", id);

        String sql = "SELECT *\n" +
                "FROM genres AS g\n" +
                "WHERE g.genre_id IN\n" +
                "    (SELECT fg.genre_id\n" +
                "     FROM film_genre AS fg\n" +
                "     WHERE fg.film_id = ?)";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeGenre(rs), id).stream().sorted(Comparator
                .comparing(Genre::getId)).collect(Collectors.toList());
    }

    public void deleteFilmGenres(Film film, List<Integer> removedFilmGenres) {
        log.info(String.format("Получен запрос на удаление жанров фильма %s", film.getName()));

        removedFilmGenres.forEach(genre -> {
            jdbcTemplate.update("DELETE FROM film_genre WHERE genre_id = ? ", genre);

            log.info("Жанр {} успешно удален у фильма {}", genre, film.getName());
        });
    }

    public void addGenresToFilm(Film film, List<Integer> addedFilmGenres) {
        addedFilmGenres.forEach(genre -> {
            jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) " + "VALUES (?, ?)",
                    film.getId(), genre);

            log.info("Жанр {} успешно добавлен фильму {}", genre, film.getName());
        });
    }

    @Override
    public void updateFilmGenres(Film film) {
        log.info("Получен запрос на добавление жанров фильму");

        if (film.getGenres() == null) {
            log.info("Жанры не были обновлены, тк нет изменений");
            return;
        }

        List<Integer> currentFilmGenres = getFilmsGenres(film.getId()).stream().map(Genre::getId).collect(Collectors.toList());
//        Set<Genre> uniqueUpdatedFilmGenres = new HashSet<>(film.getGenres());
        Set<Integer> uniqueUpdatedFilmGenres = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());

        List<Integer> removedFilmGenres = new ArrayList<>(currentFilmGenres);
        List<Integer> addedFilmGenres = new ArrayList<>(uniqueUpdatedFilmGenres);

        removedFilmGenres.removeAll(uniqueUpdatedFilmGenres);
        addedFilmGenres.removeAll(currentFilmGenres);

        if (!removedFilmGenres.isEmpty()) {
            deleteFilmGenres(film, removedFilmGenres);
        }

        if (!addedFilmGenres.isEmpty()) {
            addGenresToFilm(film, addedFilmGenres);
        }
    }

    private Genre makeGenre(ResultSet rs) throws SQLException {
        return new Genre(
                rs.getInt("genre_id"),
                rs.getString("genre"));
    }
}