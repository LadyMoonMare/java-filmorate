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
        //getReview(review.getId()); странно, однако тесты в постмане считают, что такой валидации
        // быть не должно, update не по id
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
    public void manageLikesAndDislikes(Integer id, Integer userId, String type) {
        getReview(id);
        us.findUserById(userId);

        switch (type) {
            case "addLike":
                log.info("attempt to add like to review with id = {} by user with id = {}",
                        id, userId);
                ls.addLikeToReview(id, userId);

                setUsefulForReview(true,id);
            case "addDislike":
                log.info("attempt to add dislike to review with id = {} by user with id = {}",
                        id, userId);
                ls.addDislikeToReview(id, userId);
                setUsefulForReview(false, id);
            case "deleteLike":
                log.info("attempt to remove like from review with id = {} by user with id = {}",
                        id, userId);
                ls.removeLike(id, userId);
                setUsefulForReview(false, id);
            case "deleteDislike":
                log.info("attempt to remove dislike from review with id = {} by user with id = {}",
                        id, userId);
                ls.deleteDislikeFromReview(id, userId);
                setUsefulForReview(true, id);
        }
    }

    public void setUsefulForReview(boolean isLike, Integer reviewId) {
        Review review = getReview(reviewId);
        if (isLike) {
            log.info("review id = {} get useful {} + 1", reviewId, review.getUseful());
            review.setUseful(review.getUseful() + 1);
            log.info("new useful {}", review.getUseful());
        } else {
            log.info("review id = {} get useful {} -1", reviewId, review.getUseful());
            review.setUseful(review.getUseful() - 1);
            log.info("new useful {}", review.getUseful());
        }
        rs.updateReview(review);
    }
}
