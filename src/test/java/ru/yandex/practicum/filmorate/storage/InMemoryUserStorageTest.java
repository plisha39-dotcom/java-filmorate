package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class InMemoryUserStorageTest {
    private UserStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
    }

    @Test
    void testUpdateUserPreservesFriends() {
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

        User savedUser = userStorage.findById(user.getId()).orElseThrow();

        savedUser.getFriends().add(user1.getId());

        User updatedUser = new User();
        updatedUser.setId(user.getId());
        updatedUser.setName("Новое имя");
        updatedUser.setLogin(user.getLogin());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setBirthday(user.getBirthday());

        userStorage.update(updatedUser);

        User actualUser = userStorage.findById(user.getId()).orElseThrow();

        Assertions.assertEquals(
                "Новое имя",
                actualUser.getName(),
                "Имя пользователя должно обновиться"
        );

        Assertions.assertTrue(
                actualUser.getFriends().contains(user1.getId()),
                "После обновления должен сохраниться ID друга"
        );

        Assertions.assertEquals(
                1,
                actualUser.getFriends().size(),
                "Количество друзей должно остаться равным 1"
        );
    }
}
