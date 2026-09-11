package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;


import java.util.List;

@Component
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbc;

    public EventDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Event> rowMapper = ((rs, rowNum) -> {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setTimestamp(rs.getLong("event_timestamp"));
        event.setUserId(rs.getLong("user_id"));
        event.setEventType(EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getLong("entity_id"));
        return event;
    });

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO events (event_timestamp, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId()
        );
    }

    @Override
    public List<Event> getFeedByUserId(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY event_id DESC";
        return jdbc.query(sql, rowMapper, userId);
    }
}
