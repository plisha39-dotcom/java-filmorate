package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReviewServiceTest {
    private ReviewStorage reviewStorage;
    private UserStorage userStorage;
    private FilmStorage filmStorage;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        reviewStorage = mock(ReviewStorage.class);
        userStorage = mock(UserStorage.class);
        filmStorage = mock(FilmStorage.class);
        EventService eventService = mock(EventService.class);

        service = new ReviewService(reviewStorage, userStorage, filmStorage, eventService);
    }

    @Test
    void testAddLikeWhenReviewNotFoundThrowsNotFoundException() {
        when(reviewStorage.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addLike(99L, 1L));
    }

    @Test
    void testAddLikeWhenUserNotFoundThrowsNotFoundException() {
        when(reviewStorage.findById(1L)).thenReturn(Optional.of(new Review()));
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.addLike(1L, 999L));
    }
}
