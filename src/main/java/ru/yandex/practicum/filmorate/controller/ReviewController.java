package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review createNewReview(@Valid @RequestBody Review review) {
        return reviewService.createNewReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable(value = "reviewId") Integer id) {
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReviewById(@PathVariable(value = "reviewId") Integer id) {
        reviewService.deleteReviewById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(
            @PathVariable(value = "id") Integer id,
            @PathVariable(value = "userId") Integer userId
    ) {
        reviewService.addLikeToReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(
            @PathVariable(value = "id") Integer id,
            @PathVariable(value = "userId") Integer userId
    ) {
        reviewService.addDislikeToReview(id, userId);
    }

    @GetMapping
    public List<Review> getTopFilmsByLikes(
            @RequestParam(value = "filmId", defaultValue = "0", required = false) Integer filmId,
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count
    ) {
        return reviewService.getFilmsReviews(filmId, count);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeFromReview(
            @PathVariable(value = "id") Integer id,
            @PathVariable(value = "userId") Integer userId
    ) {
        reviewService.deleteLikeFromReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeFromReview(
            @PathVariable(value = "id") Integer id,
            @PathVariable(value = "userId") Integer userId
    ) {
        reviewService.deleteDislikeFromReview(id, userId);
    }
}