package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcOperations jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<Film> getAllFilms() {
        return jdbcTemplate.query("SELECT * FROM films AS f " +
                " JOIN mpa AS m ON f.mpa_id = m.mpa_id", filmRowMapper);
    }

    @Override
    public Film addFilm(Film film) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();

        log.info("addFilm attempt for database {}",film);
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO films (title," +
                    "description, releaseDate, duration, mpa_id) VALUES (?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1,film.getName());
            ps.setObject(2,film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4,film.getDuration().toMinutes());
            ps.setObject(5,film.getMpa().getId());
            return ps;
        },kh);

        film.setId(kh.getKeyAs(Integer.class));

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update("UPDATE films SET title = ?, description = ?, releaseDate = ?," +
                        "duration = ?, mpa_id = ? WHERE id = ?;",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        return film;
    }

    @Override
    public boolean deleteFilmById(Integer id) {
        final String sql = "DELETE FROM films WHERE id = ?";
        int status = jdbcTemplate.update(sql, id);
        return status != 0;
    }

    @Override
    public Optional<Film> findFilmById(Integer id) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM films AS f" +
                " JOIN mpa AS m ON f.mpa_id = m.mpa_id WHERE id =" +
                " ?;", filmRowMapper,id));
    }

}
