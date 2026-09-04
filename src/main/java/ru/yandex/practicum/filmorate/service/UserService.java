package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        Optional<Friendship> friendship = friendshipStorage.findFriendship(friendId, userId);
        if (friendship.isPresent()) {
            friendshipStorage.confirmFriendship(friendId, userId);
        } else if (friendshipStorage.findFriendship(userId, friendId).isPresent()) {
            return;
        } else {
            friendshipStorage.addFriendship(userId, friendId);
        }
        log.info("Пользователь userId={} добавил в друзья friendId={}", user.getId(), friend.getId());
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        friendshipStorage.deleteFriendship(userId, friendId);
        log.info("Пользователь userId={} удалил из друзей friendId={}", user.getId(), friend.getId());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);
        Set<Long> commonFriendIds = new HashSet<>(friendshipStorage.getFriendsIds(user.getId()));
        commonFriendIds.retainAll(friendshipStorage.getFriendsIds(otherUser.getId()));
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
        getUserById(userId);
        Set<Long> friendIds = friendshipStorage.getFriendsIds(userId);
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
        filmStorage.removeLikesByUser(userId);
        friendshipStorage.deleteFriendshipsByUser(userId);
        userStorage.delete(userId);
        log.info("Пользователь с id {} удален вместе с лайками и друзьями", userId);
    }
}


