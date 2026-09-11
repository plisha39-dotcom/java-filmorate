package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FriendshipStorage friendshipStorage;
    private final EventService eventService;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       FriendshipStorage friendshipStorage,
                       EventService eventService) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.friendshipStorage = friendshipStorage;
        this.eventService = eventService;
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
        eventService.createEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
        log.info("Пользователь userId={} добавил в друзья friendId={}", user.getId(), friend.getId());
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        friendshipStorage.deleteFriendship(userId, friendId);
        eventService.createEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
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
        userStorage.delete(userId);
        log.info("Пользователь с id {} удален вместе с лайками и друзьями", userId);
    }

    public List<Film> getRecommendations(Long userId) {
        getUserById(userId);
        Map<Long, Set<Long>> likesByUsers = filmStorage.getLikesFromAllUsers();
        Set<Long> targetLikes = likesByUsers.getOrDefault(userId, new HashSet<>());
        if (targetLikes.isEmpty()) {
            return new ArrayList<>();
        }
        int maxIntersection = 0;
        Collection<Long> similarUserIds = new HashSet<>();
        for (Map.Entry<Long, Set<Long>> entry : likesByUsers.entrySet()) {
            Long currentUserId = entry.getKey();
            Set<Long> currentUserLikes = entry.getValue();
            if (currentUserId.equals(userId)) {
                continue;
            }
            int intersection = 0;
            for (Long filmId : targetLikes) {
                if (currentUserLikes.contains(filmId)) {
                    intersection++;
                }
            }
            if (intersection == 0) {
                continue;
            }
            if (intersection > maxIntersection) {
                maxIntersection = intersection;
                similarUserIds.clear();
                similarUserIds.add(currentUserId);
            } else if (intersection == maxIntersection) {
                similarUserIds.add(currentUserId);
            }
        }
        Set<Long> recommendationIds = new HashSet<>();
        for (Long similarUserId : similarUserIds) {
            Set<Long> similarUserLikes = likesByUsers.get(similarUserId);
            for (Long filmId : similarUserLikes) {
                if (!targetLikes.contains(filmId)) {
                    recommendationIds.add(filmId);
                }
            }
        }
        return filmStorage.getFilmsByIds(recommendationIds);
    }

    public List<Event> getFeed(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return eventService.getFeed(userId);
    }
}


