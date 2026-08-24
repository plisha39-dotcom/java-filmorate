package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Friendship;

import java.util.Optional;
import java.util.Set;

public interface FriendshipStorage {
    Optional<Friendship> findFriendship(Long requesterId, Long addresseeId);

    Friendship addFriendship(Long requesterId, Long addresseeId);

    Friendship confirmFriendship(Long requesterId, Long addresseeId);

    void deleteFriendship(Long requesterId, Long addresseeId);

    Set<Long> getFriendsIds(Long userId);
}
