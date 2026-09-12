package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class FilmControllerTest {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;
    private FilmController controller;
    private final MpaStorage mpaStorage = mock(MpaStorage.class);
    private final GenreStorage genreStorage = mock(GenreStorage.class);
    private final DirectorStorage directorStorage = mock(DirectorStorage.class);
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = mock(EventService.class);
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
        filmService = new FilmService(userStorage, filmStorage, directorStorage, eventService, mpaStorage, genreStorage);
        controller = new FilmController(filmService);
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

    @Test
    void testDeleteExistingFilm() {
        Genre mockGenre = new Genre();
        mockGenre.setId(1);
        mockGenre.setName("Комедия");
        Mockito.when(genreStorage.findById(1)).thenReturn(Optional.of(mockGenre));

        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        film.setLikes(new HashSet<>(Set.of(1L, 2L)));
        film.setGenres(new HashSet<>(Set.of(mockGenre)));

        Film createdFilm = controller.create(film);
        Long filmId = createdFilm.getId();

        controller.deleteFilm(filmId);

        assertTrue(filmStorage.findById(filmId).isEmpty(),
                "Фильм должен быть удален из хранилища");

        boolean hasOrphanLikes = filmStorage.findAll().stream()
                                            .anyMatch(f -> f.getLikes().contains(1L));
        assertFalse(hasOrphanLikes, "Связанные лайки должны быть удалены вместе с фильмом");

        boolean hasOrphanGenres = filmStorage.findAll().stream()
                                             .anyMatch(f -> f.getGenres().stream().anyMatch(g -> g.getId() == 1));
        assertFalse(hasOrphanGenres, "Связанные жанры должны быть удалены вместе с фильмом");
    }

    @Test
    void testDeleteNonExistentFilmThrowsException() {
        assertThrows(NotFoundException.class,
                () -> controller.deleteFilm(999L),
                "Удаление несуществующего фильма должно выбрасывать NotFoundException");
    }
}