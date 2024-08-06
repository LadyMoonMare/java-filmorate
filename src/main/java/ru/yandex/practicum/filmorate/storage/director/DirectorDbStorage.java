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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        return null;
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
    public void setDirectorToFilm(Film film) {
        final int directorId = film.getDirector().getId();
        final Director director = getDirectorById(directorId).orElseThrow(() -> {
            log.warn("Режиссер с id: {} не найден в базе данных", directorId);
            return new DataNotFoundException("Режиссер с id " + directorId + " не найден в базе данных");
        });
        film.setDirector(director);

        final String sql = """
                INSERT INTO film_director (film_id, director_id)
                VALUES (:filmId, :directorId)
                """;
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", film.getId());
        params.addValue("directorId", directorId);
        jdbc.update(sql, params);
    }
}
