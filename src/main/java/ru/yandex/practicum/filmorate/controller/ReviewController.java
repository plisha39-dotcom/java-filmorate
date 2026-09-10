package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {
    private final ReviewStorage reviewStorage;
    private final ReviewService reviewService;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewController(ReviewStorage reviewStorage,
                            ReviewService reviewService,
                            @Qualifier("userDbStorage") UserStorage userStorage,
                            @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.reviewService = reviewService;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());
        return reviewStorage.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review newReview) {
        if (newReview == null) {
            log.warn("Ошибка валидации: пустой отзыв");
            throw new ValidationException("Отзыв не может быть пустым");
        }
        if (newReview.getReviewId() == null) {
            log.warn("Ошибка валидации: отсутствует Id отзыва");
            throw new ValidationException("Id должен быть указан");
        }
        checkUserExists(newReview.getUserId());
        checkFilmExists(newReview.getFilmId());
        return reviewStorage.update(newReview);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewStorage.delete(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id " + id + " не найден"));
    }

    @GetMapping
    public List<Review> getReviewsByFilmId(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        return reviewService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Long id,
                           @PathVariable Long userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id,
                              @PathVariable Long userId) {
        reviewService.removeDislike(id, userId);
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
}