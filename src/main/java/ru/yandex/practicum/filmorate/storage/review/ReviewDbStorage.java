package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcOperations jo;
    private final ReviewRowMapper mapper;

    @Override
    public Review addReview(Review review) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        log.info("attempt to add review to database");

        jo.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO reviews(content, " +
                    "film_id, is_positive, useful, user_id) " +
                    "VALUES(?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, review.getContent());
            ps.setObject(2, review.getFilmId());
            ps.setObject(3, review.getPositive());
            ps.setObject(4,review.getUseful());
            ps.setObject(5, review.getUserId());
            return ps;
        }, kh);

        review.setId(kh.getKeyAs(Integer.class));

        log.info("review successfully added to database");
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        return null;
    }

    @Override
    public void deleteReview(Integer id) {
        log.info("attempt to delete review with id = {} from database",id);
        jo.update("DELETE FROM reviews WHERE id = ?;",id);
    }

    @Override
    public Review findReview(Integer id) {
        log.info("attempt to find review with id= {}", id);
        try {
            return jo.queryForObject("SELECT * FROM reviews WHERE id = ?;",mapper,id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Exception is thrown - empty result");
            log.warn("Review with id {} not found",id);
            throw  new DataNotFoundException("Review with id {} not found");
        }
    }

    @Override
    public List<Review> getAllReviews(Integer count) {
        return null;
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
        return null;
    }

    @Override
    public void addLike(Integer id, Integer userId) {

    }

    @Override
    public void addDislike(Integer id, Integer userId) {

    }

    @Override
    public void deleteLike(Integer id, Integer userId) {

    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {

    }
}
