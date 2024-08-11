package ru.yandex.practicum.filmorate.service.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewService {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Integer id);

    Review getReview(Integer id);

    List<Review> getAllReviews(Integer count);

    List<Review> getAllReviewsByFilmId(Integer filmId, Integer count);

    void manageLikesAndDislikes(Integer id, Integer userId, String type);
}
