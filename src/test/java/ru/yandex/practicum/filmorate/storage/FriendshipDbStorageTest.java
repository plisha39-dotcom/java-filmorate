package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FriendshipDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FriendshipDbStorageTest {
    private final FriendshipDbStorage friendshipStorage;
    private final UserDbStorage userStorage;

    @Test
    void testAddFriendship() {
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

        friendshipStorage.addFriendship(user.getId(), user1.getId());

        Optional<Friendship> friendship = friendshipStorage.findFriendship(user.getId(), user1.getId());

        assertThat(friendship)
                .isPresent()
                .hasValueSatisfying(foundFriendship ->
                        assertThat(foundFriendship)
                                .hasFieldOrPropertyWithValue("requesterId", user.getId())
                                .hasFieldOrPropertyWithValue("addresseeId", user1.getId())
                                .hasFieldOrPropertyWithValue("status", FriendshipStatus.UNCONFIRMED));
    }

    @Test
    void testFindFriendship() {
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

        friendshipStorage.addFriendship(user.getId(), user1.getId());

        Optional<Friendship> friendship = friendshipStorage.findFriendship(user.getId(), user1.getId());

        assertThat(friendship).isPresent();

        Optional<Friendship> friendship1 = friendshipStorage.findFriendship(user1.getId(), user.getId());

        assertThat(friendship1).isEmpty();

        assertThat(friendship)
                .hasValueSatisfying(foundFriendship ->
                        assertThat(foundFriendship)
                                .hasFieldOrPropertyWithValue("requesterId", user.getId())
                                .hasFieldOrPropertyWithValue("addresseeId", user1.getId())
                                .hasFieldOrPropertyWithValue("status", FriendshipStatus.UNCONFIRMED));
    }

    @Test
    void testConfirmFriendship() {
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

        friendshipStorage.addFriendship(user.getId(), user1.getId());

        Optional<Friendship> friendship = friendshipStorage.findFriendship(user.getId(), user1.getId());

        assertThat(friendship)
                .isPresent()
                .hasValueSatisfying(foundFriendship ->
                        assertThat(foundFriendship.getStatus())
                                .isEqualTo(FriendshipStatus.UNCONFIRMED));

        friendshipStorage.confirmFriendship(user.getId(), user1.getId());

        friendship = friendshipStorage.findFriendship(user.getId(), user1.getId());

        assertThat(friendship)
                .isPresent()
                .hasValueSatisfying(foundFriendship ->
                        assertThat(foundFriendship.getStatus())
                                .isEqualTo(FriendshipStatus.CONFIRMED));
    }

    @Test
    void testDeleteFriendship() {
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

        friendshipStorage.addFriendship(user.getId(), user1.getId());

        Optional<Friendship> friendship = friendshipStorage.findFriendship(user.getId(), user1.getId());

        assertThat(friendship).isPresent();

        friendshipStorage.deleteFriendship(user.getId(), user1.getId());

        friendship = friendshipStorage.findFriendship(user.getId(), user1.getId());

        assertThat(friendship).isEmpty();
    }

    @Test
    void testGetFriendsIds() {
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

        friendshipStorage.addFriendship(user.getId(), user1.getId());
        friendshipStorage.addFriendship(user.getId(), user2.getId());

        Set<Long> friendship = friendshipStorage.getFriendsIds(user.getId());

        assertThat(friendship).hasSize(2);
        assertThat(friendship)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    void testDeleteFriendshipsByUser() {
        User user1 = new User(); user1.setLogin("u1"); user1.setEmail("u1@t.ru"); user1.setBirthday(LocalDate.now());
        User user2 = new User(); user2.setLogin("u2"); user2.setEmail("u2@t.ru"); user2.setBirthday(LocalDate.now());
        User user3 = new User(); user3.setLogin("u3"); user3.setEmail("u3@t.ru"); user3.setBirthday(LocalDate.now());

        userStorage.create(user1); userStorage.create(user2); userStorage.create(user3);

        friendshipStorage.addFriendship(user1.getId(), user2.getId());
        friendshipStorage.addFriendship(user3.getId(), user1.getId());

        // Удаляем все связи user1
        friendshipStorage.deleteFriendshipsByUser(user1.getId());

        assertThat(friendshipStorage.findFriendship(user1.getId(), user2.getId())).isEmpty();
        assertThat(friendshipStorage.findFriendship(user3.getId(), user1.getId())).isEmpty();
    }
}

