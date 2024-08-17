package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewStorage rs;
    private final FilmStorage fs;
    private final UserStorage us;
    private final LikeStorage ls;
    private final EventStorage es;

    @Override
    public Review addReview(Review review) {
        fs.findFilmById(review.getFilmId());
        us.findUserById(review.getUserId());

        rs.addReview(review);

        Integer reviewId = rs.findReviewIdByParams(review.getFilmId(), review.getUserId());
        review = getReview(reviewId);
        addEvent(review, Operation.ADD);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        fs.findFilmById(review.getFilmId());
        us.findUserById(review.getUserId());
        Review savedReview = getReview(review.getReviewId());
//        review.setReviewId(rs.findReviewIdByParams(review.getFilmId(), review.getUserId()));
        savedReview.setContent(review.getContent());
        savedReview.setIsPositive(review.getIsPositive());
        review.setUseful(review.getUseful());
        review = rs.updateReview(savedReview);
        addEvent(review, Operation.UPDATE);
        return review;
    }

    @Override
    public void deleteReview(Integer id) {
        Review review = getReview(id);
        rs.deleteReview(id);
        addEvent(review, Operation.REMOVE);
    }

    @Override
    public Review getReview(Integer id) {
        try {
        return rs.findReview(id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Exception is thrown - empty result. Review with id {} not found", id);
            throw new DataNotFoundException("Review with id {} not found");
        }
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

        try {
            Boolean isLike = ls.getLikeReview(id, userId);

            if (!isLike) {
                review = deleteDislike(id, userId);
                review.setUseful(review.getUseful() + 1);
                log.info("dislike is discarded, adding like");
                ls.addLikeReview(id, userId);
                rs.updateReview(review);
            } else {
                throw new ValidationException("Your like is already set");
            }
        } catch (EmptyResultDataAccessException e) {
            review.setUseful(review.getUseful() + 1);
            log.info("attempt to add like to review with id = {} by user with id = {}",
                    id, userId);
            ls.addLikeReview(id, userId);
            rs.updateReview(review);
        }
    }

    @Override
    public void addDislike(Integer id, Integer userId) {
        Review review = getReview(id);
        us.findUserById(userId);

        try {
            Boolean isLike = ls.getLikeReview(id, userId);

            if (isLike) {
                review = deleteLike(id, userId);
                review.setUseful(review.getUseful() - 1);
                log.info("like is discarded, adding dislike");
                ls.addDislikeReview(id, userId);
                rs.updateReview(review);
            } else {
                throw new ValidationException("Your dislike is already set");
            }
        } catch (EmptyResultDataAccessException e) {
            review.setUseful(review.getUseful() - 1);
            log.info("attempt to add dislike to review with id = {} by user with id = {}",
                    id, userId);
            ls.addDislikeReview(id, userId);
            rs.updateReview(review);
        }
    }

    @Override
    public Review deleteLike(Integer id, Integer userId) {
        Review review = getReview(id);
        us.findUserById(userId);

        try {
            Boolean isLike = ls.getLikeReview(id, userId);

            if (isLike) {
                review.setUseful(review.getUseful() - 1);
                log.info("attempt to remove like from review with id = {} by user with id = {}",
                        id, userId);
                ls.deleteLikeReview(id, userId);
                rs.updateReview(review);
                return review;
            } else {
                throw new DataNotFoundException("there is no like on review id" + id);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("there is no like on review id" + id);
        }
    }

    @Override
    public Review deleteDislike(Integer id, Integer userId) {
        Review review = getReview(id);
        us.findUserById(userId);

        try {
            Boolean isLike = ls.getLikeReview(id, userId);

            if (!isLike) {
                review.setUseful(review.getUseful() + 1);
                log.info("attempt to remove dislike from review with id = {} by user with id = {}",
                        id, userId);
                ls.deleteDislikeReview(id, userId);
                rs.updateReview(review);
                return review;
            } else {
                throw  new DataNotFoundException("there is no dislike on review id" + id);
            }
        } catch (EmptyResultDataAccessException e) {
            throw  new DataNotFoundException("there is no dislike on review id" + id);
        }
    }

    public void addEvent(Review review, Operation operation) {
        Event event = new Event();
        event.setTimestamp(Instant.now().toEpochMilli());
        event.setEventType(EventType.REVIEW);
        event.setOperation(operation);
        event.setUserId(review.getUserId());
        event.setEntityId(review.getReviewId());
        es.addEvent(event);
    }
}
