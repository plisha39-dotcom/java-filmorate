package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(Long id);

    Optional<Review> findById(Long id);

    List<Review> findReviewsByFilmId(Long filmId, int count);

    void addLikeDislike(Long reviewId, Long userId, boolean isLike);

    void removeLikeDislike(Long reviewId, Long userId, boolean isLike);
}
