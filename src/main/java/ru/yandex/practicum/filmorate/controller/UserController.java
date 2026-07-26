package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> allUsers() {
        return userStorage.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    @GetMapping("/{id}/friends")
    public Collection<User> allFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> commonFriends(@PathVariable Long id,
                                          @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser == null) {
            log.warn("Ошибка валидации: пустой пользователь");
            throw new ValidationException("Пользователь не может быть пустым");
        }
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации: отсутствует Id пользователя");
            throw new ValidationException("Id должен быть указан");
        }
        validateUser(newUser);
        return userStorage.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id,
                          @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id,
                             @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
    }

    private void validateUser(User user) {
        if (user == null) {
            log.warn("Ошибка валидации: пустой пользователь");
            throw new ValidationException("Пользователь не может быть пустым");
        }
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: эл.почта пустая или отсутствует @");
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: пустой логин или содержатся пробелы");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя отсутствует, используется логин");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: неверная дата рождения");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
