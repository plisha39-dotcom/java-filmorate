package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Friendship {
    private Long requesterId;
    private Long addresseeId;
    private FriendshipStatus status;
}
