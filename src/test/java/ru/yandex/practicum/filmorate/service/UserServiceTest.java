package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public class UserServiceTest {
    private UserStorage userStorage;
    private UserService userService;
    private FilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        userService = new UserService(userStorage, filmStorage);
    }

    @Test
    void testAddFriendAddsUsersToEachOther() {
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

        userService.addFriend(user.getId(), user1.getId());

        User savedUser = userStorage.findById(user.getId()).orElseThrow();
        User savedUser1 = userStorage.findById(user1.getId()).orElseThrow();

        Assertions.assertTrue(savedUser.getFriends().contains(user1.getId()),
                "У первого пользователя в друзьях должен быть второй пользователь");
        Assertions.assertTrue(savedUser1.getFriends().contains(user.getId()),
                "У второго пользователя в друзьях должен быть первый пользователь");
        Assertions.assertEquals(1, savedUser.getFriends().size(), "Размер списка друзей должен быть 1");
        Assertions.assertEquals(1, savedUser1.getFriends().size(), "Размер списка друзей должен быть 1");
    }

    @Test
    void testAddFriendTwiceDoesNotCreateDuplicate() {
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

        userService.addFriend(user.getId(), user1.getId());
        userService.addFriend(user.getId(), user1.getId());

        User savedUser = userStorage.findById(user.getId()).orElseThrow();
        User savedUser1 = userStorage.findById(user1.getId()).orElseThrow();

        Assertions.assertTrue(savedUser.getFriends().contains(user1.getId()),
                "У первого пользователя в друзьях должен быть второй пользователь");
        Assertions.assertTrue(savedUser1.getFriends().contains(user.getId()),
                "У второго пользователя в друзьях должен быть первый пользователь");
        Assertions.assertEquals(1, savedUser.getFriends().size(), "Размер списка друзей должен быть 1");
        Assertions.assertEquals(1, savedUser1.getFriends().size(), "Размер списка друзей должен быть 1");
    }

    @Test
    void testRemoveFriendRemovesUsersFromEachOther() {
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

        userService.addFriend(user.getId(), user1.getId());

        userService.removeFriend(user.getId(), user1.getId());

        User savedUser = userStorage.findById(user.getId()).orElseThrow();
        User savedUser1 = userStorage.findById(user1.getId()).orElseThrow();

        Assertions.assertTrue(
                savedUser.getFriends().isEmpty(),
                "Список друзей первого пользователя должен быть пустым"
        );
        Assertions.assertTrue(
                savedUser1.getFriends().isEmpty(),
                "Список друзей второго пользователя должен быть пустым"
        );
    }

    @Test
    void testGetFriendsReturnsUserFriends() {
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

        userService.addFriend(user.getId(), user1.getId());
        userService.addFriend(user.getId(), user2.getId());

        List<User> friends = userService.getFriends(user.getId());

        Assertions.assertEquals(2, friends.size(), "Список друзей должен равняться 2");
        Assertions.assertTrue(
                friends.contains(user1),
                "В списке друзей должен быть первый друг"
        );

        Assertions.assertTrue(
                friends.contains(user2),
                "В списке друзей должен быть второй друг"
        );
        Assertions.assertFalse(
                friends.contains(user),
                "В списке друзей не должен содержаться сам основной пользователь"
        );
    }

    @Test
    void testGetCommonFriendsReturnsOnlyMutualFriends() {
        User userA = new User();
        userA.setName("Борис");
        userA.setLogin("BOR");
        userA.setEmail("bor@yandex.ru");
        userA.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(userA);

        User userB = new User();
        userB.setName("Иван");
        userB.setLogin("ivan");
        userB.setEmail("ivan@yandex.ru");
        userB.setBirthday(LocalDate.of(2000, 12, 15));

        userStorage.create(userB);

        User user1 = new User();
        user1.setName("Вася");
        user1.setLogin("Vasya");
        user1.setEmail("vs@yandex.ru");
        user1.setBirthday(LocalDate.of(2001, 6, 1));

        userStorage.create(user1);

        User user2 = new User();
        user2.setName("Аня");
        user2.setLogin("Ann");
        user2.setEmail("ann@yandex.ru");
        user2.setBirthday(LocalDate.of(2001, 6, 1));

        userStorage.create(user2);

        userService.addFriend(userA.getId(), user1.getId());
        userService.addFriend(userA.getId(), user2.getId());
        userService.addFriend(userB.getId(), user1.getId());

        Collection<User> friends = userService.getCommonFriends(userA.getId(), userB.getId());

        Assertions.assertEquals(1, friends.size(),
                "Размер списка общих друзей должен быть равен 1");
        Assertions.assertTrue(friends.contains(user1), "В списке должен быть один общий друг");
        Assertions.assertFalse(friends.contains(user2),
                "В общем списке друзей, должен отсутствовать друг первого пользователя");
    }

    @Test
    void testAddFriendThrowsNotFoundExceptionWhenUserDoesNotExist() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        Assertions.assertThrows(NotFoundException.class,
                () -> userService.addFriend(user.getId(), 999L),
                "При добавлении несуществующего друга должно выбрасываться NotFoundException");

        User savedUser = userStorage.findById(user.getId()).orElseThrow();

        Assertions.assertTrue(
                savedUser.getFriends().isEmpty(),
                "Список друзей первого пользователя должен быть пустым"
        );
    }

    @Test
    void testDeleteUserRemovesLikesFromFilms() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        User savedUser = userStorage.create(user);

        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Film savedFilm = filmStorage.create(film);

        savedFilm.getLikes().add(savedUser.getId());
        filmStorage.update(savedFilm);

        Assertions.assertTrue(
                filmStorage.findById(savedFilm.getId()).orElseThrow()
                        .getLikes().contains(savedUser.getId()),
                "Перед удалением пользователя его лайк должен находиться у фильма"
        );

        userService.deleteUser(savedUser.getId());

        Assertions.assertTrue(
                userStorage.findById(savedUser.getId()).isEmpty(),
                "Пользователь должен быть удалён"
        );

        Assertions.assertFalse(
                filmStorage.findById(savedFilm.getId()).orElseThrow()
                        .getLikes().contains(savedUser.getId()),
                "Лайки удалённого пользователя должны быть очищены"
        );
    }
}
