package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

public class FilmServiceTest {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    private FilmService filmService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(userStorage, filmStorage);

    }

    @Test
    void testAddLikeAddsUserIdToFilmLikes() {
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

        filmService.addLike(film.getId(), user.getId());

        Film savedFilm = filmStorage.findById(film.getId()).orElseThrow();

        Assertions.assertTrue(savedFilm.getLikes().contains(user.getId()),
                "У фильма должен быть 1 лайк от пользователя");
        Assertions.assertEquals(1, savedFilm.getLikes().size(),
                "Количество лайков у фильма == 1");
    }

    @Test
    void testAddLikeTwiceDoesNotCreateDuplicate() {
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

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user.getId());

        Film savedFilm = filmStorage.findById(film.getId()).orElseThrow();

        Assertions.assertTrue(savedFilm.getLikes().contains(user.getId()),
                "У фильма должен быть 1 лайк от пользователя");
        Assertions.assertEquals(1, savedFilm.getLikes().size(),
                "Количество лайков у фильма == 1");
    }

    @Test
    void testRemoveLikeRemovesUserIdFromFilmLikes() {
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

        filmService.addLike(film.getId(), user.getId());

        filmService.removeLike(film.getId(), user.getId());

        Film savedFilm = filmStorage.findById(film.getId()).orElseThrow();

        Assertions.assertTrue(
                savedFilm.getLikes().isEmpty(),
                "Список лайков должен быть пустым"
        );
    }

    @Test
    void testGetPopularFilmsReturnsFilmsSortedByLikesAndLimitedByCount() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        User user1 = new User();
        user1.setName("Иван");
        user1.setLogin("ivan");
        user1.setEmail("ivan@yandex.ru");
        user1.setBirthday(LocalDate.of(2000, 12, 15));

        userStorage.create(user1);

        User user2 = new User();
        user2.setName("Вася");
        user2.setLogin("Vasya");
        user2.setEmail("vs@yandex.ru");
        user2.setBirthday(LocalDate.of(2001, 6, 1));

        userStorage.create(user2);

        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        filmStorage.create(film);

        Film film1 = new Film();
        film1.setName("Начало");
        film1.setDescription("Новый фильм");
        film1.setReleaseDate(LocalDate.of(2000, 11, 11));
        film1.setDuration(150);

        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Новый фильм2");
        film2.setDescription("Новый фильм 2");
        film2.setReleaseDate(LocalDate.of(1999, 2, 2));
        film2.setDuration(140);

        filmStorage.create(film2);

        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());

        List<Film> films = filmService.getPopularFilms(2);

        Assertions.assertEquals(2, films.size(), "Список из популярных фильмов должен == 2");
        Assertions.assertEquals(
                film.getId(),
                films.get(0).getId(),
                "Первым должен быть фильм с двумя лайками"
        );

        Assertions.assertEquals(
                film1.getId(),
                films.get(1).getId(),
                "Вторым должен быть фильм с одним лайком"
        );

        Assertions.assertFalse(
                films.contains(film2),
                "Фильм без лайков не должен попасть в результат при count = 2"
        );
    }

    @Test
    void testAddLikeThrowsNotFoundExceptionWhenUserDoesNotExist() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        filmStorage.create(film);

        Assertions.assertThrows(NotFoundException.class,
                () -> filmService.addLike(film.getId(), 999L),
                "При добавлении лайка от несуществующего пользователя должен выбрасываться NotFoundException"
        );


        Assertions.assertTrue(
                filmStorage.findById(film.getId()).orElseThrow().getLikes().isEmpty(),
                "Список лайков должен быть пустым"
        );
    }

    @Test
    void testAddLikeThrowsNotFoundExceptionWhenFilmDoesNotExist() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        Assertions.assertThrows(NotFoundException.class,
                () -> filmService.addLike(999L, user.getId()),
                "При пустом фильме должен выброситься NotFoundException");
    }

    @Test
    void testGetPopularFilmsThrowsValidationExceptionWhenCountIsNegative() {
        Assertions.assertThrows(ValidationException.class,
                () -> filmService.getPopularFilms(-1),
                "При отрицательном количестве фильмов должна выбрасываться ValidationException");
    }

    @Test
    void testGetCommonFilmsThrowsNotFoundExceptionWhenFriendDoesNotExist() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        Assertions.assertThrows(NotFoundException.class,
                () -> filmService.getCommonFilms(user.getId(), 999L),
                "При отсутствии второго пользователя должен выброситься NotFoundException");
    }
}
