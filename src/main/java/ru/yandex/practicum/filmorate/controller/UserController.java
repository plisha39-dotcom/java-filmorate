package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
}


/* public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> allUsers() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        for (User newUser : users.values()) {
            if (newUser.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        User oldUser = users.get(newUser.getId());
        if (oldUser == null) {
            throw new NotFoundException("Пользователь отсутствует");
        }
        if (newUser.getEmail() != null) {
            for (User user : users.values()) {
                if (user.getEmail().equals(newUser.getEmail())
                        && !user.getId().equals(newUser.getId())) {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                }
            }
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getUsername() != null) {
            oldUser.setUsername(newUser.getUsername());
        }
        if (newUser.getPassword() != null) {
            oldUser.setPassword(newUser.getPassword());
        }
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
*/