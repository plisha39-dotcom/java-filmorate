package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserControllerTest {
    UserStorage userStorage;
    UserController controller;
    UserService userService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
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
    void testCreateUserWhenEmailIsBlankThrowsValidationException() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        assertThrows(
                ValidationException.class,
                () -> controller.create(user),
                "Пользователь с пустым email должен выбрасывать ValidationException"
        );
    }

    @Test
    void testCreateUserWhenEmailWithoutAtThrowsValidationException() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("boryandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        assertThrows(
                ValidationException.class,
                () -> controller.create(user),
                "Пользователь с отсутствующей @ должен выбрасывать ValidationException"
        );
    }

    @Test
    void testCreateUserWhenLoginIsBlankThrowsValidationException() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        assertThrows(
                ValidationException.class,
                () -> controller.create(user),
                "Пользователь с пустым логином должен выбрасывать ValidationException"
        );
    }

    @Test
    void testCreateUserWhenLoginContainsSpaceThrowsValidationException() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("B R");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        assertThrows(
                ValidationException.class,
                () -> controller.create(user),
                "Пользователь с логином содержащим пробел должен выбрасывать ValidationException"
        );
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
    void testCreateUserWhenBirthdayIsInFutureThrowsValidationException() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(
                ValidationException.class,
                () -> controller.create(user),
                "Пользователь с неверной датой рождения должен выбрасывать ValidationException"
        );
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
    void testCreateUserWhenUserIsNullThrowsValidationException() {
        assertThrows(
                ValidationException.class,
                () -> controller.create(null),
                "Пустой пользователь должен выбрасывать ValidationException"
        );
    }

    @Test
    void testUpdateUserWhenIdIsNullThrowsValidationException() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        assertThrows(
                ValidationException.class,
                () -> controller.update(user),
                "Пользователь без id должен выбрасывать ValidationException"
        );
    }
}
