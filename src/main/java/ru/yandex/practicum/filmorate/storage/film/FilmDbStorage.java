package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

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
    public void deleteFilmById(Integer id) {
        final String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Film addFilm(Film film) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();

        log.info("addFilm attempt for database {}", film);
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO films (title," +
                            "description, releaseDate, duration, mpa_id) VALUES (?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4, film.getDuration().toMinutes());
            ps.setObject(5, film.getMpa().getId());
            return ps;
        }, kh);

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
    public Optional<Film> findFilmById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM films AS f" +
                    " JOIN mpa AS m ON f.mpa_id = m.mpa_id WHERE id =" +
                    " ?;", filmRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            log.warn("Film with id {} not found", id);
            throw new DataNotFoundException("Film with id {} not found");
        }
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        // Определяем поле для сортировки
        String sortField;
        if (sortBy.equals("year")) {
            sortField = "f.releaseDate";
        } else {
            sortField = "like_count";
        }

        log.info("Поле для сортировки {}", sortField);

        //SQL-запрос с динамическим ORDER BY в зависимости от запроса
        final String sql = String.format("""
                SELECT f.id, f.title, f.description, f.releaseDate, f.duration, f.mpa_id, mpa.rating, l.like_count
                FROM films AS f
                JOIN film_director AS fd ON f.id = fd.film_id
                JOIN mpa ON f.mpa_id = mpa.mpa_id
                LEFT JOIN (
                    SELECT film_id, COUNT(*) AS like_count
                    FROM likes
                    GROUP BY film_id
                    ) l ON f.id = l.film_id
                WHERE fd.director_id = ?
                ORDER BY %s DESC
                """, sortField);

        return jdbcTemplate.query(sql, new FilmRowMapper(), directorId);
    }

    @Override
    public List<Film> searchFilmsByParameter(String query, boolean searchByTitle, boolean searchByDirector) {
        final String sqlSearchByTitle = """
                SELECT f.id, f.title, f.description, f.releaseDate, f.duration, f.mpa_id, mpa.rating, COUNT(l.user_id) AS likes
                FROM films f
                JOIN mpa ON f.mpa_id = mpa.mpa_id
                LEFT JOIN likes l ON f.id = l.film_id
                WHERE LOWER(f.title) LIKE LOWER(?)
                GROUP BY f.id, f.title, f.description, f.releaseDate, f.duration, f.mpa_id, mpa.rating
                ORDER BY likes DESC
                """;

        final String sqlSearchByDirector = """
                SELECT f.id, f.title, f.description, f.releaseDate, f.duration, f.mpa_id, mpa.rating, COUNT(l.user_id) AS likes
                FROM films f
                JOIN mpa ON f.mpa_id = mpa.mpa_id
                LEFT JOIN film_director fd ON f.id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.id
                LEFT JOIN likes l ON f.id = l.film_id
                WHERE LOWER(d.name) LIKE LOWER(?)
                GROUP BY f.id, f.title, f.description, f.releaseDate, f.duration, f.mpa_id, mpa.rating
                ORDER BY likes DESC
                """;

        final String sqlSearchByTitleAndDirector = """
                SELECT f.id, f.title, f.description, f.releaseDate, f.duration, f.mpa_id, mpa.rating, COUNT(l.user_id) AS likes
                FROM films f
                JOIN mpa ON f.mpa_id = mpa.mpa_id
                LEFT JOIN film_director fd ON f.id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.id
                LEFT JOIN likes l ON f.id = l.film_id
                WHERE LOWER(f.title) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?)
                GROUP BY f.id, f.title, f.description, f.releaseDate, f.duration, f.mpa_id, mpa.rating
                ORDER BY likes DESC
                """;

        if (searchByTitle && searchByDirector) {
            return jdbcTemplate.query(sqlSearchByTitleAndDirector, filmRowMapper, "%" + query + "%", "%" + query + "%");
        } else if (searchByTitle) {
            return jdbcTemplate.query(sqlSearchByTitle, filmRowMapper, "%" + query + "%");
        } else {
            return jdbcTemplate.query(sqlSearchByDirector, filmRowMapper, "%" + query + "%");
        }
    }
}
