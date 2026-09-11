package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;

public class UserServiceTest {
    private UserStorage userStorage;
    private UserService userService;
    private FilmStorage filmStorage;
    private FriendshipStorage friendshipStorage;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        filmStorage = new InMemoryFilmStorage();
        friendshipStorage = Mockito.mock(FriendshipStorage.class);
        eventService = mock(EventService.class);
        userService = new UserService(userStorage, filmStorage, friendshipStorage,eventService);
    }

    @Test
    void testCreatingNewOneWayConnection() {
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

        Mockito.when(friendshipStorage.findFriendship(user1.getId(), user.getId()))
               .thenReturn(Optional.empty());
        Mockito.when(friendshipStorage.findFriendship(user.getId(), user1.getId()))
               .thenReturn(Optional.empty());

        userService.addFriend(user.getId(), user1.getId());

        Mockito.verify(friendshipStorage).addFriendship(user.getId(), user1.getId());
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

        Friendship friendship = new Friendship();
        friendship.setRequesterId(user.getId());
        friendship.setAddresseeId(user1.getId());
        friendship.setStatus(FriendshipStatus.UNCONFIRMED);

        Mockito.when(friendshipStorage.findFriendship(user.getId(), user1.getId()))
               .thenReturn(Optional.empty()).thenReturn(Optional.of(friendship));

        userService.addFriend(user.getId(), user1.getId());
        userService.addFriend(user.getId(), user1.getId());

        Mockito.verify(friendshipStorage, Mockito.times(1)).addFriendship(user.getId(), user1.getId());
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

        userService.removeFriend(user.getId(), user1.getId());

        Mockito.verify(friendshipStorage, Mockito.times(1))
               .deleteFriendship(user.getId(), user1.getId());
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

        Mockito.when(friendshipStorage.getFriendsIds(user.getId()))
               .thenReturn(Set.of(user1.getId(), user2.getId()));

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

        Mockito.when(friendshipStorage.getFriendsIds(userA.getId()))
               .thenReturn(Set.of(user1.getId(), user2.getId()));
        Mockito.when(friendshipStorage.getFriendsIds(userB.getId()))
               .thenReturn(Set.of(user1.getId()));

        Collection<User> friends = userService.getCommonFriends(userA.getId(), userB.getId());

        Assertions.assertEquals(1, friends.size(),
                "Размер списка общих друзей должен быть равен 1");
        Assertions.assertTrue(friends.contains(user1), "В списке должен быть один общий друг");
        Assertions.assertFalse(friends.contains(user2),
                "В общем списке друзей, должен отсутствовать друг первого пользователя");
    }

    @Test
    void testAddFriendWithUnknownFriendThrowsNotFoundException() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        assertThrows(
                NotFoundException.class,
                () -> userService.addFriend(user.getId(), 999L),
                "При добавлении несуществующего друга должно выбрасываться NotFoundException"
        );

        Mockito.verify(friendshipStorage, Mockito.never())
               .addFriendship(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void testRecommendationsReturnUniqueMoviesFromMostSimilarUsers() {
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

        User user3 = new User();
        user3.setName("Имя");
        user3.setLogin("Фамилия");
        user3.setEmail("email");
        user3.setBirthday(LocalDate.of(2001, 6, 1));

        userStorage.create(user3);

        Mpa mpa = new Mpa();
        mpa.setId(1);

        Film commonFilm1 = new Film();
        commonFilm1.setName("Интерстеллар");
        commonFilm1.setDescription("Общий фильм 1");
        commonFilm1.setReleaseDate(LocalDate.of(2014, 11, 6));
        commonFilm1.setDuration(169);
        commonFilm1.setMpa(mpa);
        filmStorage.create(commonFilm1);

        Film commonFilm2 = new Film();
        commonFilm2.setName("Начало");
        commonFilm2.setDescription("Общий фильм 2");
        commonFilm2.setReleaseDate(LocalDate.of(2010, 7, 16));
        commonFilm2.setDuration(148);
        commonFilm2.setMpa(mpa);
        filmStorage.create(commonFilm2);

        Film filmFromB = new Film();
        filmFromB.setName("Матрица");
        filmFromB.setDescription("Фильм пользователя B");
        filmFromB.setReleaseDate(LocalDate.of(1999, 3, 31));
        filmFromB.setDuration(136);
        filmFromB.setMpa(mpa);
        filmStorage.create(filmFromB);

        Film sharedFilm = new Film();
        sharedFilm.setName("Дюна");
        sharedFilm.setDescription("Фильм пользователей B и C");
        sharedFilm.setReleaseDate(LocalDate.of(2021, 10, 22));
        sharedFilm.setDuration(155);
        sharedFilm.setMpa(mpa);
        filmStorage.create(sharedFilm);

        Film filmFromC = new Film();
        filmFromC.setName("Прибытие");
        filmFromC.setDescription("Фильм пользователя C");
        filmFromC.setReleaseDate(LocalDate.of(2016, 11, 11));
        filmFromC.setDuration(116);
        filmFromC.setMpa(mpa);
        filmStorage.create(filmFromC);

        Film filmFromD = new Film();
        filmFromD.setName("Гладиатор");
        filmFromD.setDescription("Фильм менее похожего пользователя D");
        filmFromD.setReleaseDate(LocalDate.of(2000, 5, 5));
        filmFromD.setDuration(155);
        filmFromD.setMpa(mpa);
        filmStorage.create(filmFromD);

        filmStorage.addLike(commonFilm1.getId(), user.getId());
        filmStorage.addLike(commonFilm2.getId(), user.getId());
        filmStorage.addLike(commonFilm1.getId(), user1.getId());
        filmStorage.addLike(commonFilm2.getId(), user1.getId());
        filmStorage.addLike(filmFromB.getId(), user1.getId());
        filmStorage.addLike(sharedFilm.getId(), user1.getId());
        filmStorage.addLike(commonFilm1.getId(), user2.getId());
        filmStorage.addLike(commonFilm2.getId(), user2.getId());
        filmStorage.addLike(sharedFilm.getId(), user2.getId());
        filmStorage.addLike(filmFromC.getId(), user2.getId());
        filmStorage.addLike(commonFilm1.getId(), user3.getId());
        filmStorage.addLike(filmFromD.getId(), user3.getId());

        List<Film> recommendations = userService.getRecommendations(user.getId());

        assertThat(recommendations)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(
                        filmFromB.getId(),
                        sharedFilm.getId(),
                        filmFromC.getId()
                );
    }

    @Test
    void testRecommendationsReturnEmptyListWhenUserHasNoLikes() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        List<Film> recommendations = userService.getRecommendations(user.getId());

        assertThat(recommendations).isEmpty();
    }

    @Test
    void testRecommendationsThrowNotFoundExceptionWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.getRecommendations(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testDeleteUserSuccess() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));
        userStorage.create(user);

        userService.deleteUser(user.getId());

        assertThrows(NotFoundException.class, () -> userService.getFriends(user.getId()),
                "Удаленный пользователь не должен находиться");
    }

    @Test
    void testGetFeedThrowNotFoundExceptionWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.getFeed(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
