package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserService(@Qualifier("userDbStorage")UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        userStorage.update(user);
        userStorage.update(friend);
        log.info("Пользователь userId={} добавил в друзья friendId={}", user.getId(), friend.getId());
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        userStorage.update(user);
        userStorage.update(friend);
        log.info("Пользователь userId={} удалил из друзей friendId={}", user.getId(), friend.getId());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);
        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());
        List<User> userList = new ArrayList<>();
        for (Long friendId : commonFriendIds) {
            User friend = getUserById(friendId);
            userList.add(friend);
        }
        log.debug(
                "Получены общие друзья пользователей userId={} и otherId={}: count={}",
                userId,
                otherId,
                userList.size()
        );
        return userList;
    }

    private User getUserById(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        Set<Long> friendIds = user.getFriends();
        List<User> friends = new ArrayList<>();
        for (Long friendId : friendIds) {
            User friend = getUserById(friendId);
            friends.add(friend);
        }
        log.debug(
                "Получены друзья пользователя userId={}: count={}",
                userId,
                friends.size()
        );
        return friends;
    }

    public void deleteUser(Long userId) {
        getUserById(userId);
        for (Film film : filmStorage.findAll()) {
            if (film.getLikes().remove(userId)) {
                filmStorage.update(film);
            }
        }
        userStorage.delete(userId);
    }
}


