package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.film.review.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;

    public Review createNewReview(Review review) {
        return reviewStorage.createNewReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReviewById(int id) {
        reviewStorage.deleteReviewById(id);
    }

    public Review getReviewById(int id) {
        return reviewStorage.getReviewById(id);
    }

    public List<Review> getFilmsReviews(int id, int count) {
        return reviewStorage.getFilmsReviews(id, count);
    }

    public void addLikeToReview(int reviewId, int userId) {
        reviewStorage.addLikeToReview(reviewId, userId);
    }

    public void addDislikeToReview(int reviewId, int userId) {
        reviewStorage.addDislikeToReview(reviewId, userId);
    }

    public void deleteLikeFromReview(int reviewId, int userId) {
        reviewStorage.deleteLikeFromReview(reviewId, userId);
    }

    public void deleteDislikeFromReview(int reviewId, int userId) {
        reviewStorage.deleteDislikeFromReview(reviewId, userId);
    }
}