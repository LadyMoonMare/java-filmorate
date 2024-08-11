package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcOperations jdbcTemplate;
    private final GenreRowMapper grm;
    private  final Comparator<Genre> comparator = new Comparator<Genre>() {
        @Override
        public int compare(Genre o1, Genre o2) {
            return o1.getId() - o2.getId();
        }
    };

    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT * FROM genres", grm);
    }

    public Optional<Genre> findGenreById(Integer id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject("SELECT * FROM genres WHERE id =" +
                    " ?;", grm, id));
        } catch (EmptyResultDataAccessException e) {
            log.warn("genre with id {} not found",id);
            throw  new DataNotFoundException("genre with id {} not found");
        }
    }

    @Override
    public List<Genre> getGenresByFilmId(Integer filmId) {
        return jdbcTemplate.query("SELECT * FROM genres AS g JOIN film_genre AS fg " +
                "ON g.id = fg.genre_id WHERE film_id = ?;", grm, filmId);
    }

    @Override
    public void addFilmGenre(Integer filmId, Integer genreId) {
        jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?,?);", filmId,
                genreId);
    }

    @Override
    public void removeFilmGenre(Integer filmId) {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?;",filmId);
    }

    @Override
    public Integer getNumberOfGenres() {
        return jdbcTemplate.queryForObject("SELECT COUNT(id) FROM genres;",Integer.class);
    }

    @Override
    public List<Film> loadGenres(List<Film> films) {
        Map<Integer, Genre> genres = new HashMap<>();
        Map<Integer, Film> f = new HashMap<>();
        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>());
            f.put(film.getId(), film);
        });
        getAllGenres().forEach(genre -> genres.put(genre.getId(), genre));
        jdbcTemplate.query("SELECT * FROM film_genre",
                (rs) -> {
                    while (rs.next()) {
                        Integer filmId = rs.getInt("film_id");
                        /*Добавил тут проверку. Возникает NullPointerException если мы
                        присваиваем жанры не абсолютно всем фильмам из БД
                        а только некоторой выборке (например по режиссеру). Тогда фильма
                        с искомым id может не оказаться в Map<Integer, Film> f
                         */
                        Film film = f.get(filmId);
                        if (film != null) {
                            film.getGenres().add(genres.get(rs.getInt("genre_id")));
                        }
                    }

                });
        f.values().forEach(film -> {
            film.setGenres(new LinkedHashSet<>(film.getGenres().stream()
                    .sorted(comparator).collect(Collectors.toSet())));
        });
        return new ArrayList<>(f.values());
    }

    @Override
    public Film setGenresToFilm(Film film) {
        Map<Integer, Genre> genres = new HashMap<>();
        getAllGenres().forEach(genre -> genres.put(genre.getId(), genre));
        List<Genre> newGenres = new ArrayList<>(film.getGenres());
        film.setGenres(new LinkedHashSet<>());
        newGenres.forEach(genre -> {
            if (!genres.containsKey(genre.getId())) {
                log.warn("genre with id {} not found",genre.getId());
                throw  new DataNotFoundException("genre with id {} not found");
            } else {
                film.getGenres().add(genres.get(genre.getId()));
            }
        });

        jdbcTemplate.batchUpdate("INSERT INTO film_genre (film_id, genre_id) VALUES (?,?);",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, film.getId());
                        ps.setInt(2,newGenres.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return newGenres.size();
                    }
                });
        return film;
    }

}
