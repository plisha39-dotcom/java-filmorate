package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectorServiceTest {
    private DirectorDbStorage directorStorage;
    private DirectorService directorService;

    @BeforeEach
    void setUp() {
        directorStorage = mock(DirectorDbStorage.class);
        directorService = new DirectorService(directorStorage);
    }

    @Test
    void testGetDirectorByIdNotFound() {
        when(directorStorage.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> directorService.getDirectorById(99));
    }

    @Test
    void testUpdateDirectorWithoutIdThrowsException() {
        Director director = new Director();
        director.setName("Режиссер без ID");

        assertThrows(ValidationException.class,
                () -> directorService.update(director),
                "Обновление режиссера без id должно выбрасывать ValidationException");
    }
}
