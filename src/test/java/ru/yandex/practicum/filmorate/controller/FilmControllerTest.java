package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;
    private FilmController controller;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(userStorage, filmStorage);
        controller = new FilmController(filmStorage, filmService);
    }

    @Test
    void testCreateFilmWhenFilmIsValidReturnsFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Film createdFilm = controller.create(film);

        assertEquals(1L, createdFilm.getId(), "Первому фильму должен быть присвоен id = 1");
        assertEquals("Интерстеллар", createdFilm.getName(), "Название фильма должно сохраниться");
        assertEquals("Фантастический фильм", createdFilm.getDescription(), "Описание фильма должно сохраниться");
        assertEquals(LocalDate.of(2014, 11, 6), createdFilm.getReleaseDate(), "Дата релиза должна сохраниться");
        assertEquals(169, createdFilm.getDuration(), "Продолжительность должна сохраниться");
    }

    @Test
    void testCreateFilmWhenDescriptionLengthIs200ReturnsFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("A".repeat(200));
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Film createdFilm = controller.create(film);

        assertEquals(1L, createdFilm.getId(), "Первому фильму должен быть присвоен id = 1");
        assertEquals("Интерстеллар", createdFilm.getName(), "Название фильма должно сохраниться");
        assertEquals("A".repeat(200), createdFilm.getDescription(), "Описание фильма должно сохраниться");
        assertEquals(LocalDate.of(2014, 11, 6), createdFilm.getReleaseDate(), "Дата релиза должна сохраниться");
        assertEquals(169, createdFilm.getDuration(), "Продолжительность должна сохраниться");
    }

    @Test
    void testCreateFilmWhenReleaseDateIs18951228ReturnsFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(169);

        Film createdFilm = controller.create(film);

        assertEquals(1L, createdFilm.getId(), "Первому фильму должен быть присвоен id = 1");
        assertEquals("Интерстеллар", createdFilm.getName(), "Название фильма должно сохраниться");
        assertEquals("Фантастический фильм", createdFilm.getDescription(), "Описание фильма должно сохраниться");
        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate(), "Дата релиза должна сохраниться");
        assertEquals(169, createdFilm.getDuration(), "Продолжительность должна сохраниться");
    }

    @Test
    void testUpdateFilmWhenFilmExistsReturnsUpdatedFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Старое описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Film createdFilm = controller.create(film);

        Long idFilm = createdFilm.getId();

        Film newFilm = new Film();
        newFilm.setId(idFilm);
        newFilm.setName("Начало");
        newFilm.setDescription("Новое описание");
        newFilm.setReleaseDate(LocalDate.of(2010, 1, 8));
        newFilm.setDuration(120);

        Film updateFilm = controller.update(newFilm);

        assertEquals(idFilm, updateFilm.getId(), "Id фильма не должен измениться при обновлении");
        assertEquals("Начало", updateFilm.getName(), "Название фильма должно измениться");
        assertEquals("Новое описание", updateFilm.getDescription(), "Описание фильма должно измениться");
        assertEquals(LocalDate.of(2010, 1, 8), updateFilm.getReleaseDate(), "Дата релиза должна измениться");
        assertEquals(120, updateFilm.getDuration(), "Продолжительность должна измениться");
    }
}