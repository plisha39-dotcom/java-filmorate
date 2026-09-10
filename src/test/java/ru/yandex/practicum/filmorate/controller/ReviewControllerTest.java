package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReviewControllerTest {
    private ReviewService reviewService;
    private ReviewController controller;

    @BeforeEach
    void setUp() {
        reviewService = mock(ReviewService.class);
        controller = new ReviewController(reviewService);
    }

    @Test
    void testCreateReview() {
        Review review = new Review();
        review.setContent("Классный фильм");
        when(reviewService.create(any(Review.class))).thenReturn(review);

        assertEquals("Классный фильм", controller.create(review).getContent());
        verify(reviewService, times(1)).create(review);
    }

    @Test
    void testUpdateReviewSuccess() {
        Review review = new Review();
        review.setReviewId(1L);
        review.setContent("Обновленный текст");
        when(reviewService.update(any(Review.class))).thenReturn(review);

        assertEquals("Обновленный текст", controller.update(review).getContent());
        verify(reviewService, times(1)).update(review);
    }

    @Test
    void testDeleteReview() {
        assertDoesNotThrow(() -> controller.delete(1L));
        verify(reviewService, times(1)).delete(1L);
    }
}
