package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventServiceTest {

    @Test
    void testGetFeedWhenUserNotFoundThrowsNotFoundException() {
        EventStorage eventStorage = mock(EventStorage.class);
        UserStorage userStorage = mock(UserStorage.class);
        EventService eventService = new EventService(eventStorage, userStorage);

        when(userStorage.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getFeed(99L));
    }
}