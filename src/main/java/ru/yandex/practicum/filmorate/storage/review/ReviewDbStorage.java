package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcOperations jo;

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

        return review;
    }

    @Override
    public Review updateReview(Review review) {
        return null;
    }

    @Override
    public void deleteReview(Integer id) {

    }

    @Override
    public Review getReview(Integer id) {
        return  null;
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
