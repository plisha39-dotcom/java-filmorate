package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public EventService(EventStorage eventStorage,
                        @Qualifier("userDbStorage") UserStorage userStorage) {
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    public void createEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        Event event = new Event();
        event.setTimestamp(Instant.now().toEpochMilli());
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);

        eventStorage.addEvent(event);
    }

    public List<Event> getFeed(Long userId) {
        userStorage.findById(userId);
        return eventStorage.getFeedByUserId(userId);
    }

}
