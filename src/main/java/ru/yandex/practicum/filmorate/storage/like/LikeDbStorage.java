package ru.yandex.practicum.filmorate.storage.like;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {
    private final JdbcOperations jdbcTemplate;
    private final UserRowMapper urm;

    @Override
    public List<User> getLikesFromDb(Integer id) {
        return jdbcTemplate.query("SELECT * FROM app_users AS u JOIN likes AS l " +
                "ON u.id = l.user_id JOIN films AS f ON l.film_id = f.id" +
                " WHERE f.id =?;",urm,id);
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
    public void addLikeToReview(Integer id, Integer userId) {
        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?,?);"
                , id, userId, true);
    }

    @Override
    public void addDislikeToReview(Integer id, Integer userId) {

    }

    @Override
    public void deleteLikeFromReview(Integer id, Integer userId) {

    }

    @Override
    public void deleteDislikeFromReview(Integer id, Integer userId) {

    }

}
