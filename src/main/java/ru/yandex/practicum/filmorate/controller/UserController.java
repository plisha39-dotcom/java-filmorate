package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> allUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь добавлен: id={}", user.getId());
        return user;
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
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            log.warn("Ошибка обновления: пользователь с id {} не найден", newUser.getId());
            throw new ValidationException("Пользователь с таким id не найден");
        }
        validateUser(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Пользователь обновлен: id={}", newUser.getId());
        return newUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
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
