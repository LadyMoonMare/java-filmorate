package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Collection<Director> getAllDirectors() {
        final String sql = """
                SELECT *
                FROM directors
                """;
        final List<Director> directors = jdbc.query(sql, new DirectorRowMapper());
        log.debug("Получили всех режиссеров из БД, размер списка: {}", directors.size());
        return directors;
    }

    @Override
    public Director addDirector(Director director) {
        final String sql = """
                INSERT INTO directors (name)
                VALUES (:name)
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", director.getName());
        log.debug("Добавляем режиссера в БД. Параметры SQL запроса: {}", params);
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        //Получаем id из БД, устанавливаем его объекту режиссера и логируем
        int id = keyHolder.getKey().intValue();
        director.setId(id);
        log.debug("Режиссеру присвоен в БД id: {}", id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        final String sql = """
                UPDATE directors
                SET name = :name
                WHERE id = :id
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", director.getId());
        params.addValue("name", director.getName());
        log.debug("Обновляем информацию о режиссере в БД. Параметры SQL запроса: {}", params);
        jdbc.update(sql, params);
        return director;
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        final String sql = """
                SELECT *
                FROM directors
                WHERE id = :id
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        log.debug("Ищем в БД режиссера с id: {}", id);
        return jdbc.query(sql, params, new DirectorRowMapper()).stream().findFirst();
    }

    @Override
    public void deleteDirector(int id) {
        final String sql = """
                DELETE FROM directors
                WHERE id = :id
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        log.debug("Удаляем из БД режиссера с id: {}", id);
        jdbc.update(sql, params);
    }

    @Override
    public void setDirectorsToFilm(Film film) {
        final Map<Integer, Director> allDirectors = getAllDirectors().stream()
                .collect(Collectors.toMap(Director::getId, director -> director));

        final Set<Director> newDirectors = film.getDirectors();
        film.setDirectors(new HashSet<>());

        final List<MapSqlParameterSource> batchParams = new ArrayList<>();
        newDirectors.forEach(director -> {
            int directorId = director.getId();
            if (!allDirectors.containsKey(directorId)) {
                log.warn("Режиссер с id: {} не найден в БД", directorId);
                throw new DataNotFoundException("Режиссер с id " + directorId + " не найден в базе данных.");
            } else {
                film.getDirectors().add(allDirectors.get(directorId));
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("filmId", film.getId());
                params.addValue("directorId", directorId);
                batchParams.add(params);
            }
        });

        log.info("Добавляем связи 'фильм - режиссеры' в БД: {}", batchParams);

        final String sql = """
                INSERT INTO film_director (film_id, director_id)
                VALUES (:filmId, :directorId)
                """;
        jdbc.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }

    @Override
    public void removeFilmDirector(int id) {
        final String sql = """
                DELETE FROM film_director
                WHERE film_id = :id
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        jdbc.update(sql, params);
    }

    @Override
    public List<Film> loadDirectors(List<Film> films) {
        //Мапим список фильмов в список их id
        final List<Integer> filmIds = films.stream().map(Film::getId).toList();

        // Получение всех связей между фильмами, которые есть в списке, и режиссерами
        final String getFilmDirectorRelationsSql = """
                SELECT film_id, director_id
                FROM film_director
                WHERE film_id IN (:filmIds)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmIds", filmIds);

        //Для каждой связи создаем объект класса FilmDirectorRelation и кладем их в List
        final List<FilmDirectorRelation> filmDirectorRelations = jdbc.query(getFilmDirectorRelationsSql, params,
                (resultSet, rowNum) -> //лямбда реализует метод RowMapper для объектов FilmDirectorRelation
                        new FilmDirectorRelation(resultSet.getInt("film_id"), resultSet.getInt("director_id")));
        log.info("Получили все связи фильм-режиссер из БД, размер списка: {}", filmDirectorRelations.size());

        //Получаем список уникальных id режиссеров для запрошенных фильмов
        final List<Integer> directorIds = filmDirectorRelations.stream()
                .map(relation -> relation.directorId).distinct().toList();

        // Получение режиссеров по списку их id
        final String getDirectorsSql = """
                SELECT id, name
                FROM directors
                WHERE id IN (:directorIds)
                """;
        params = new MapSqlParameterSource();
        params.addValue("directorIds", directorIds);
        final List<Director> directors = jdbc.query(getDirectorsSql, params, new DirectorRowMapper());

        // Создаем мапы для быстрого доступа к фильмам и режиссерам по их id
        final Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));
        final Map<Integer, Director> directorMap = directors.stream().collect(Collectors.toMap(Director::getId, director -> director));

        // Добавление режиссеров к соответствующим фильмам
        for (FilmDirectorRelation relation : filmDirectorRelations) {
            Film film = filmMap.get(relation.filmId());
            if (film != null) {
                if (film.getDirectors() == null) {
                    film.setDirectors(new HashSet<>());
                }
                Director director = directorMap.get(relation.directorId());
                if (director != null) {
                    film.getDirectors().add(director);
                }
            }
        }
        return films;
    }

    // Класс для представления связи между фильмом и режиссером
    private record FilmDirectorRelation(int filmId, int directorId) {
    }
}
