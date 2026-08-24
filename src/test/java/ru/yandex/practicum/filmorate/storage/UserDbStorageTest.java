package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void testFindUserById() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        Optional<User> optionalUser = userStorage.findById(user.getId());

        assertThat(optionalUser)
                .isPresent()
                .hasValueSatisfying(foundUser ->
                        assertThat(foundUser).hasFieldOrPropertyWithValue("id", user.getId())
                                .hasFieldOrPropertyWithValue("name", user.getName())
                                .hasFieldOrPropertyWithValue("login", user.getLogin())
                                .hasFieldOrPropertyWithValue("email", user.getEmail())
                                .hasFieldOrPropertyWithValue("birthday", user.getBirthday())
                );
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        assertThat(user.getId()).isNotNull();

        assertThat(userStorage.findById(user.getId())).isPresent();
    }

    @Test
    void testFindAllUsers() {
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

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
        assertThat(users)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(user.getId(), user1.getId());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        User updatedUser = new User();
        updatedUser.setId(user.getId());
        updatedUser.setName("Новое имя");
        updatedUser.setLogin(user.getLogin());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setBirthday(user.getBirthday());

        userStorage.update(updatedUser);

        Optional<User> optionalUser = userStorage.findById(user.getId());

        assertThat(optionalUser)
                .isPresent()
                .hasValueSatisfying(foundUser -> assertThat(foundUser).hasFieldOrPropertyWithValue("id", updatedUser.getId())
                        .hasFieldOrPropertyWithValue("name", updatedUser.getName()));
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        Long userId = user.getId();

        userStorage.delete(userId);
        Optional<User> optionalUser = userStorage.findById(userId);

        assertThat(optionalUser).isEmpty();
    }
}