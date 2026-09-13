package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DirectorControllerTest {
    private DirectorService directorService;
    private DirectorController controller;

    @BeforeEach
    void setUp() {
        directorService = mock(DirectorService.class);
        controller = new DirectorController(directorService);
    }

    @Test
    void testGetAllDirectors() {
        when(directorService.getAllDirectors()).thenReturn(List.of(new Director()));
        assertEquals(1, controller.getAllDirectors().size());
    }

    @Test
    void testGetDirectorByIdSuccess() {
        Director director = new Director();
        director.setId(1);
        director.setName("Нолан");
        when(directorService.getDirectorById(1)).thenReturn(director);

        Director result = controller.getDirectorById(1);
        assertEquals("Нолан", result.getName());
    }

    @Test
    void testGetDirectorByIdNotFound() {
        when(directorService.getDirectorById(99)).thenThrow(new NotFoundException("Не найден"));
        assertThrows(NotFoundException.class, () -> controller.getDirectorById(99));
    }

    @Test
    void testCreateDirector() {
        Director director = new Director();
        director.setName("Спилберг");
        when(directorService.create(any(Director.class))).thenReturn(director);

        Director result = controller.create(director);
        assertEquals("Спилберг", result.getName());
        verify(directorService, times(1)).create(director);
    }

    @Test
    void testUpdateDirectorSuccess() {
        Director director = new Director();
        director.setId(1);
        director.setName("Новое имя");
        when(directorService.update(any(Director.class))).thenReturn(director);

        Director result = controller.update(director);
        assertEquals("Новое имя", result.getName());
    }

    @Test
    void testDeleteDirector() {
        doNothing().when(directorService).delete(1);
        assertDoesNotThrow(() -> controller.delete(1));
        verify(directorService, times(1)).delete(1);
    }
}