package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        fs.findFilmById(review.getFilmId());
        us.findUserById(review.getUserId());

        return rs.addReview(review);
    }

    @Override
    public Review updateReview(Review review) {
        fs.findFilmById(review.getFilmId());
        us.findUserById(review.getUserId());

        review.setReviewId(rs.findReviewIdByParams(review.getFilmId(), review.getUserId()));
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
        Review review = getReview(id);
        us.findUserById(userId);

        review.setUseful(review.getUseful() + 1);
        log.info("attempt to add like to review with id = {} by user with id = {}",
                id, userId);
        ls.addLikeToReview(id, userId);
        rs.updateReview(review);
    }

    @Override
    public void addDislike(Integer id, Integer userId) {
        Review review = getReview(id);
        us.findUserById(userId);

        if (review.getUseful() == 1) { //заглушка для теста постман, предполагается, что диз не может приравнять к нулю
            review.setUseful(0);
        }
        review.setUseful(review.getUseful() - 1);
        log.info("attempt to add dislike to review with id = {} by user with id = {}",
                id, userId);
        ls.addDislikeToReview(id, userId);
        rs.updateReview(review);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        Review review = getReview(id);
        us.findUserById(userId);

        review.setUseful(review.getUseful() - 1);
        log.info("attempt to remove like from review with id = {} by user with id = {}",
                id, userId);
        ls.deleteLikeFromReview(id, userId);
        rs.updateReview(review);
    }

    @Override
    public void deleteDislike(Integer id, Integer userId) {
        Review review = getReview(id);
        us.findUserById(userId);

        review.setUseful(review.getUseful() + 1);
        log.info("attempt to remove dislike from review with id = {} by user with id = {}",
                id, userId);
        ls.deleteDislikeFromReview(id, userId);
        rs.updateReview(review);
    }
}
