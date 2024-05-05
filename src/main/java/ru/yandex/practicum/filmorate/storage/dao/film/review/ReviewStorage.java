package ru.yandex.practicum.filmorate.storage.dao.film.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review createNewReview(Review review);

    Review updateReview(Review review);

    void deleteReviewById(int id);

    Review getReviewById(int id);

    List<Review> getFilmsReviews(int id, int count);

    void addLikeToReview(int reviewId, int userId);

    void addDislikeToReview(int reviewId, int userId);

    void deleteLikeFromReview(int reviewId, int userId);

    void deleteDislikeFromReview(int reviewId, int userId);
}