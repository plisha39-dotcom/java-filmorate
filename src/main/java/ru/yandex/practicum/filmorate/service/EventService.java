package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class EventService {
    private final EventStorage eventStorage;

    public EventService(EventStorage eventStorage) {
        this.eventStorage = eventStorage;
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
        return eventStorage.getFeedByUserId(userId);
    }
}
