package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReviewControllerTest {
    private ReviewStorage reviewStorage;
    private ReviewController controller;

    @BeforeEach
    void setUp() {
        reviewStorage = mock(ReviewStorage.class);
        controller = new ReviewController(reviewStorage, mock(ReviewService.class), null, null);
    }

    @Test
    void testCreateReview() {
        Review review = new Review();
        review.setContent("Классный фильм");
        when(reviewStorage.create(any(Review.class))).thenReturn(review);

        assertEquals("Классный фильм", controller.create(review).getContent());
        verify(reviewStorage, times(1)).create(review);
    }

    @Test
    void testUpdateReviewSuccess() {
        Review review = new Review();
        review.setReviewId(1L);
        review.setContent("Обновленный текст");

        when(reviewStorage.update(any(Review.class))).thenReturn(review);

        Review result = controller.update(review);
        assertEquals("Обновленный текст", result.getContent());
        verify(reviewStorage, times(1)).update(review);
    }

    @Test
    void testDeleteReview() {
        assertDoesNotThrow(() -> controller.deleteReview(1L));
        verify(reviewStorage, times(1)).delete(1L);
    }
}
