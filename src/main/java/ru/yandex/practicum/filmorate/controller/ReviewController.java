package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService rs;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("attempt to add review");
        return rs.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("attempt to update review");
        return rs.updateReview(review);
    }

    @Validated
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        log.info("attempt to delete review");
        rs.deleteReview(id);
    }

    @Validated
    @GetMapping("/{id}")
    public Review getReview(@PathVariable Integer id) {
        log.info("attempt to get review by id = {}", id);
        return rs.getReview(id);
    }

    @Validated
    @GetMapping()
    public List<Review> getAllReviews(@RequestParam (required = false) @Positive Integer filmId,
                                      @RequestParam(defaultValue = "10") @Positive Integer count) {
        if (filmId == null) {
            log.info("filmId is null. attempt to get all reviews");
            return rs.getAllReviews(count);
        } else {
            log.info("attempt to get all reviews about film number {}", filmId);
            return rs.getAllReviewsByFilmId(filmId, count);
        }
    }

    @Validated
    @PutMapping("/{id}/like/{userId}") //is useful
    public void addLike(@PathVariable @Positive Integer id, @PathVariable @Positive Integer userId) {
        log.info("attempt to add like to review with id = {}, by user {}", id, userId);
        rs.addLike(id, userId);
    }

    @Validated
    @PutMapping("{id}/dislike/{userId}") //is useless
    public void addDislike(@PathVariable @Positive Integer id, @PathVariable @Positive Integer userId) {
        log.info("attempt to add dislike to review with id = {}, by user {}", id, userId);
        rs.addDislike(id, userId);
    }

    @Validated
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable @Positive Integer id, @PathVariable @Positive Integer userId) {
        log.info("attempt to delete like from review with id = {}, by user {}", id, userId);
        rs.deleteLike(id, userId);
    }

    @Validated
    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable @Positive Integer id,
                              @PathVariable @Positive Integer userId) {
        log.info("attempt to delete dislike from review with id = {}, by user {}", id, userId);
        rs.deleteDislike(id, userId);
    }

}
