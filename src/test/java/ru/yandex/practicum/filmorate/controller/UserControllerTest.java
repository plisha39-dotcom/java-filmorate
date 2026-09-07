package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

public class UserControllerTest {
    private UserStorage userStorage;
    private UserController controller;
    private UserService userService;
    private FilmStorage filmStorage;
    private FriendshipStorage friendshipStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        friendshipStorage = Mockito.mock(FriendshipStorage.class);
        userService = new UserService(userStorage, filmStorage, friendshipStorage);
        controller = new UserController(userStorage, userService);
    }

    @Test
    void testCreateUserWhenUserIsValidReturnsUser() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        User createdUser = controller.create(user);

        assertEquals(1L, createdUser.getId(), "Первому пользователю должен быть присвоен id = 1");
        assertEquals("Борис", createdUser.getName(), "Имя пользователя должно сохраниться");
        assertEquals("BOR", createdUser.getLogin(), "Логин пользователя должен сохраниться");
        assertEquals(LocalDate.of(1999, 1, 15), createdUser.getBirthday(), "Дата рождения должна сохраниться");
        assertEquals("bor@yandex.ru", createdUser.getEmail(), "email должен сохраниться");
    }

    @Test
    void testCreateUserWhenNameIsBlankUsesLoginAsName() {
        User user = new User();
        user.setName("");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        User createdUser = controller.create(user);

        assertEquals(1L, createdUser.getId(), "Первому пользователю должен быть присвоен id = 1");
        assertEquals("BOR", createdUser.getName(), "Имя пользователя должно поменяться на логин");
        assertEquals("BOR", createdUser.getLogin(), "Логин пользователя должен сохраниться");
        assertEquals(LocalDate.of(1999, 1, 15), createdUser.getBirthday(), "Дата рождения должна сохраниться");
        assertEquals("bor@yandex.ru", createdUser.getEmail(), "email должен сохраниться");
    }

    @Test
    void testCreateUserWhenBirthdayIsTodayReturnsUser() {
        LocalDate today = LocalDate.now();

        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(today);

        User createdUser = controller.create(user);

        assertEquals(1L, createdUser.getId(), "Первому пользователю должен быть присвоен id = 1");
        assertEquals("Борис", createdUser.getName(), "Имя пользователя должно сохраниться");
        assertEquals("BOR", createdUser.getLogin(), "Логин пользователя должен сохраниться");
        assertEquals(today, createdUser.getBirthday(), "Дата рождения должна сохраниться");
        assertEquals("bor@yandex.ru", createdUser.getEmail(), "email должен сохраниться");
    }

    @Test
    void testUpdateUserWhenUserExistsReturnsUpdatedUser() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        User createdUser = controller.create(user);

        Long idUser = createdUser.getId();

        User newUser = new User();
        newUser.setId(idUser);
        newUser.setName("Новое имя");
        newUser.setLogin("Новый_логин");
        newUser.setEmail("new@yandex.ru");
        newUser.setBirthday(LocalDate.of(1990, 2, 20));

        User updateUser = controller.update(newUser);

        assertEquals(idUser, updateUser.getId(), "Id пользователя не должен измениться");
        assertEquals("Новое имя", updateUser.getName(), "Имя пользователя должно измениться");
        assertEquals("Новый_логин", updateUser.getLogin(), "Логин пользователя должен измениться");
        assertEquals(LocalDate.of(1990, 2, 20), updateUser.getBirthday(), "Дата рождения должна измениться");
        assertEquals("new@yandex.ru", updateUser.getEmail(), "email должен измениться");
    }

    @Test
    void testDeleteExistingUser() {
        User user = new User();
        user.setLogin("testuser");
        user.setEmail("test@test.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = controller.create(user);

        User friend = new User();
        friend.setLogin("friend");
        friend.setEmail("friend@test.com");
        friend.setBirthday(LocalDate.of(1990, 1, 1));
        User createdFriend = controller.create(friend);

        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        Friendship friendship = new Friendship();
        friendship.setRequesterId(createdUser.getId());
        friendship.setAddresseeId(createdFriend.getId());
        Mockito.when(friendshipStorage.findFriendship(createdUser.getId(), createdFriend.getId()))
                .thenReturn(Optional.of(friendship));

        controller.deleteUser(createdUser.getId());

        assertTrue(userStorage.findById(createdUser.getId()).isEmpty(),
                "Пользователь должен быть удален из хранилища");

        Film updatedFilm = filmStorage.findById(createdFilm.getId()).orElseThrow();
        assertFalse(updatedFilm.getLikes().contains(createdUser.getId()),
                "Лайк удаленного пользователя должен быть удален из фильма");

        verify(friendshipStorage).deleteFriendshipsByUser(createdUser.getId());
    }

    @Test
    void testDeleteNonExistentUserThrowsException() {
        assertThrows(NotFoundException.class,
                () -> controller.deleteUser(999L),
                "Удаление несуществующего пользователя должно выбрасывать NotFoundException");
    }
}
