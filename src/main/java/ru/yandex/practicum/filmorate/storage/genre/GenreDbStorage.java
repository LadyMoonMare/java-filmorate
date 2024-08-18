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
        int rowAffected = jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?,?);", filmId,
                genreId);
        log.info("Добавлена связь фильм-жанры film_id: {}, genre_id: {}, вставлено строк: {}", filmId, genreId, rowAffected);
    }

    @Override
    public void removeFilmGenre(Integer filmId) {
        int rowsAffected = jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?;",filmId);
        log.info("Удалили связи фильм-жанры для фильма с id: {}. Удалено {} строк", filmId, rowsAffected);
    }

    @Override
    public Integer getNumberOfGenres() {
        return jdbcTemplate.queryForObject("SELECT COUNT(id) FROM genres;",Integer.class);
    }

    @Override
    public List<Film> loadGenres(List<Film> films) {
        //Мапим список фильмов в список их id
        final List<Integer> filmIds = films.stream().map(Film::getId).toList();
        log.info("Ищем жанры фильмов с id: {} для добавления их в фильм", filmIds);

        final String getFilmGenreRelationsSql = """
                SELECT film_id, genre_id
                FROM film_genre
                """;
        final List<FilmGenreRelation> filmGenreRelations = jdbcTemplate.query(getFilmGenreRelationsSql,
                (resultSet, rowNum) -> //лямбда реализует метод RowMapper для объектов FilmGenreRelation
                        new FilmGenreRelation(resultSet.getInt("film_id"), resultSet.getInt("genre_id")));
        log.info("Список связей фильм - жанры через record: {}", filmGenreRelations);

        //Получаем список уникальных id жанров для запрошенных фильмов
        final List<Integer> genreIds = filmGenreRelations.stream()
                .map(relation -> relation.genreId)
                .distinct()
                .toList();

        // Получение жанров по списку их id
        final String getGenresSql = """
                SELECT id, name
                FROM genres
                """;
        final List<Genre> genres = jdbcTemplate.query(getGenresSql, grm);

        // Создаем мапы для быстрого доступа к фильмам и жанрам по их id
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        final Map<Integer, Genre> genreMap = genres.stream().collect(Collectors.toMap(Genre::getId, genre -> genre));

        // Добавление жанров к соответствующим фильмам
        filmMap.forEach((id, film) -> film.setGenres(new LinkedHashSet<>()));
        for (FilmGenreRelation relation : filmGenreRelations) {
            Film film = filmMap.get(relation.filmId());
            if (film != null) {
                Genre genre = genreMap.get(relation.genreId());
                if (genre != null) {
                    film.getGenres().add(genre);
                }
            }
        }
        return films;
    }

    private record FilmGenreRelation(int filmId, int genreId) {
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
                        log.info("Сохраняем в БД новые связи фильм-жанры film_id: {}, genre_id: {}", film.getId(), newGenres.get(i).getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return newGenres.size();
                    }
                });
        return film;
    }

}
