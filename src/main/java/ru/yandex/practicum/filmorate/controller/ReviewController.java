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
    public Review getReviewById(@PathVariable(value = "reviewId") int id) {
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReviewById(@PathVariable(value = "reviewId") int id) {
        reviewService.deleteReviewById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(
            @PathVariable(value = "id") int id,
            @PathVariable(value = "userId") int userId
    ) {
        reviewService.addLikeToReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(
            @PathVariable(value = "id") int id,
            @PathVariable(value = "userId") int userId
    ) {
        reviewService.addDislikeToReview(id, userId);
    }

    @GetMapping
    public List<Review> getTopFilmsByLikes(
            @RequestParam(value = "filmId", defaultValue = "0", required = false) int filmId,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count
    ) {
        return reviewService.getFilmsReviews(filmId, count);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeFromReview(
            @PathVariable(value = "id") int id,
            @PathVariable(value = "userId") int userId
    ) {
        reviewService.deleteLikeFromReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeFromReview(
            @PathVariable(value = "id") int id,
            @PathVariable(value = "userId") int userId
    ) {
        reviewService.deleteDislikeFromReview(id, userId);
    }
}