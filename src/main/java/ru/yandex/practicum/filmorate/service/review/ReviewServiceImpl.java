package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage rs;
    private final FilmStorage fs;
    private final UserStorage us;
    private final LikeStorage ls;

    @Override
    public Review addReview(Review review) {
        isValid(review.getFilmId(), review.getUserId());
        review.setUseful(0);
        return rs.addReview(review);
    }

    @Override
    public Review updateReview(Review review) {
        getReview(review.getId());
        return rs.updateReview(review);
    }

    @Override
    public void deleteReview(Integer id) {
        getReview(id);
        rs.deleteReview(id);
    }

    @Override
    public Review getReview(Integer id) {
        return rs.findReview(id);
    }

    @Override
    public List<Review> getAllReviews(Integer count) {
        return rs.getAllReviews(count);
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Integer filmId, Integer count) {
        fs.findFilmById(filmId);
        return rs.getAllReviewsByFilmId(filmId, count);
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        getReview(id);
        us.findUserById(userId);
        log.info("attempt to add like to review with id = {} by user with id = {}", id, userId);
        ls.addLikeToReview(id, userId);

        log.info("review id = {} get useful +1", id);
        Review review = getReview(id);
        review.setUseful(getReview(id).getUseful() + 1 );
        rs.updateReview(review);
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

    public void isValid(Integer filmId, Integer userId) {
        try {
            fs.findFilmById(filmId);
            us.findUserById(userId);
        } catch (DataNotFoundException e) {
            throw new ValidationException("Invalid film or user id for review");
        }
    }
}
