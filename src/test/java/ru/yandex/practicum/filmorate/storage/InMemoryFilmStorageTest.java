package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class InMemoryFilmStorageTest {
    private FilmStorage filmStorage;
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
    }

    @Test
    void testUpdateFilmPreservesLikes() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        filmStorage.create(film);

        Film savedFilm = filmStorage.findById(film.getId()).orElseThrow();

        savedFilm.getLikes().add(user.getId());

        Film updateFilm = new Film();
        updateFilm.setId(film.getId());
        updateFilm.setName("Новый фильм");
        updateFilm.setDescription(film.getDescription());
        updateFilm.setReleaseDate(film.getReleaseDate());
        updateFilm.setDuration(film.getDuration());

        filmStorage.update(updateFilm);

        Film actualFilm = filmStorage.findById(film.getId()).orElseThrow();

        Assertions.assertEquals(
                "Новый фильм",
                actualFilm.getName(),
                "Название фильма должно обновиться"
        );

        Assertions.assertTrue(
                actualFilm.getLikes().contains(user.getId()),
                "После обновления должен сохраниться ID пользователя, поставившего лайк"
        );

        Assertions.assertEquals(
                1,
                actualFilm.getLikes().size(),
                "Количество лайков должно остаться равным 1"
        );
    }
}
