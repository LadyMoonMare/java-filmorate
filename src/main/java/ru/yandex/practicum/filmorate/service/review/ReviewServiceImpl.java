package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
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
        return  rs.findReview(id);
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

    public void isValid(Integer filmId, Integer userId) {
        try {
            fs.findFilmById(filmId);
            us.findUserById(userId);
        } catch (DataNotFoundException e) {
            throw new ValidationException("Invalid film or user id for review");
        }
    }
}
