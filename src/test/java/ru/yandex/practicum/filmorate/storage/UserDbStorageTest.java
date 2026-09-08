package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, FilmDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbc;

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

    @Test
    void testDeleteUserCascadesInDb() {
        User userToDelete = new User();
        userToDelete.setName("Удаляемый");
        userToDelete.setLogin("delete_me");
        userToDelete.setEmail("delete@yandex.ru");
        userToDelete.setBirthday(LocalDate.of(1999, 1, 15));
        userStorage.create(userToDelete);
        Long userIdToDelete = userToDelete.getId();

        User friendUser = new User();
        friendUser.setName("Друг");
        friendUser.setLogin("friend");
        friendUser.setEmail("friend@yandex.ru");
        friendUser.setBirthday(LocalDate.of(1999, 1, 15));
        userStorage.create(friendUser);
        Long friendId = friendUser.getId();

        Film film = new Film();
        film.setName("Фильм для лайка");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(120);
        filmStorage.create(film);
        Long filmId = film.getId();

        filmStorage.addLike(filmId, userIdToDelete);
        jdbc.update("insert into friendship (requester_id, addressee_id, status_id) values (?, ?, 1)",
                userIdToDelete, friendId);

        Integer likesBefore = jdbc.queryForObject(
                "select count(*) from film_likes where user_id = ?", Integer.class, userIdToDelete);
        Integer friendshipsBefore = jdbc.queryForObject(
                "select count(*) from friendship where requester_id = ? or addressee_id = ?",
                Integer.class, userIdToDelete, userIdToDelete);

        assertThat(likesBefore).isEqualTo(1);
        assertThat(friendshipsBefore).isEqualTo(1);

        userStorage.delete(userIdToDelete);

        assertThat(userStorage.findById(userIdToDelete)).isEmpty();

        Integer likesAfter = jdbc.queryForObject(
                "select count(*) from film_likes where user_id = ?", Integer.class, userIdToDelete);
        Integer friendshipsAfter = jdbc.queryForObject(
                "select count(*) from friendship where requester_id = ? or addressee_id = ?",
                Integer.class, userIdToDelete, userIdToDelete);

        assertThat(likesAfter).isZero();
        assertThat(friendshipsAfter).isZero();
    }
}