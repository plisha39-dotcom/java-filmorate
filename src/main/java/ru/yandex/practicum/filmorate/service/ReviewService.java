package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
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