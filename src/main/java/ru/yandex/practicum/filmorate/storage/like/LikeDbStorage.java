package ru.yandex.practicum.filmorate.storage.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {

    private final JdbcOperations jdbcTemplate;
    private final UserRowMapper urm;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<User> getLikesFromDb(Integer id) {
        return jdbcTemplate.query("SELECT * FROM app_users AS u JOIN likes AS l " +
                "ON u.id = l.user_id JOIN films AS f ON l.film_id = f.id" +
                " WHERE f.id =?;",urm,id);
    }

    @Override
    public List<Film> getFilmLikes(Integer userId) {
        return jdbcTemplate.query("""
                SELECT f.*, m.rating
                FROM films AS f
                JOIN mpa AS m ON f.mpa_id = m.mpa_id
                JOIN likes AS l ON f.id = l.film_id
                JOIN app_users AS u ON l.user_id = u.id
                WHERE u.id =?;
                """, filmRowMapper, userId);
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?,?);", id, userId);
    }

    @Override
    public void removeLike(Integer id, Integer userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?",id,userId);
    }

    @Override
    public void addLikeReview(Integer id, Integer userId) {
        jdbcTemplate.update("INSERT INTO like_reviews (review_id, user_id, is_like) VALUES (?,?," +
                        " ?);", id, userId, true);
    }

    @Override
    public void addDislikeReview(Integer id, Integer userId) {
        jdbcTemplate.update("INSERT INTO like_reviews (review_id, user_id, is_like) VALUES (?,?," +
                        " ?);", id, userId, false);
    }

    @Override
    public void deleteLikeReview(Integer id, Integer userId) {
        jdbcTemplate.update("DELETE FROM like_reviews WHERE review_id = ? AND user_id = ? " +
                "AND is_like = ?", id, userId,true);
    }

    @Override
    public void deleteDislikeReview(Integer id, Integer userId) {
        jdbcTemplate.update("DELETE FROM like_reviews WHERE review_id = ? AND user_id = ? " +
                "AND is_like = ?", id, userId, false);
    }

    @Override
    public Boolean getLikeReview(Integer id, Integer userId) {
        return jdbcTemplate.queryForObject("SELECT is_like FROM like_reviews WHERE " +
                "review_id = ? AND user_id = ?;", Boolean.class, id, userId);
    }
}
