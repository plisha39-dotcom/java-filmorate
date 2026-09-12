package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         EventService eventService) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventService = eventService;
    }

    public Review create(Review review) {
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());
        Review createdReview = reviewStorage.create(review);

        eventService.createEvent(createdReview.getUserId(), EventType.REVIEW, Operation.ADD, createdReview.getReviewId());

        return createdReview;
    }

    public Review update(Review review) {
        if (review == null) {
            throw new ValidationException("Отзыв не может быть пустым");
        }
        if (review.getReviewId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        Review oldReview = getReviewId(review.getReviewId());
        if (review.getUserId() == null) {
            review.setUserId(oldReview.getUserId());
        }
        if (review.getFilmId() == null) {
            review.setFilmId(oldReview.getFilmId());
        }
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());
        Review updatedReview = reviewStorage.update(review);
        eventService.createEvent(updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE, updatedReview.getReviewId());
        return updatedReview;
    }

    public void delete(Long id) {
        checkReviewExists(id);
        Review deletedReview = getReviewId(id);
        eventService.createEvent(deletedReview.getUserId(), EventType.REVIEW, Operation.REMOVE, id);
        reviewStorage.delete(id);
    }

    public Review getReviewId(Long id) {
        return reviewStorage.findById(id)
                            .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        if (filmId != null) {
            checkFilmExists(filmId);
        }
        return reviewStorage.findReviewsByFilmId(filmId, count);
    }

    public void addLike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewStorage.addLikeDislike(reviewId, userId, true);
    }

    public void addDislike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewStorage.addLikeDislike(reviewId, userId, false);
    }

    public void removeLike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewStorage.removeLikeDislike(reviewId, userId, true);
    }

    public void removeDislike(Long reviewId, Long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);
        reviewStorage.removeLikeDislike(reviewId, userId, false);
    }

    private void checkUserExists(Long userId) {
        if (userId != null && userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
    }

    private void checkFilmExists(Long filmId) {
        if (filmId != null && filmStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
    }

    private void checkReviewExists(Long reviewId) {
        if (reviewId != null && reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
    }
}