package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({EventDbStorage.class,
        UserDbStorage.class,
        FilmDbStorage.class,
        MpaDbStorage.class,
        GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventDbStorageTest {

    private final EventDbStorage eventStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Test
    void testAddLikeEvent() {
        User user = createTestUser();
        Film film = createTestFilm();

        Event event = new Event();
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(user.getId());
        event.setEventType(EventType.LIKE);
        event.setOperation(Operation.ADD);
        event.setEntityId(film.getId());

        eventStorage.addEvent(event);

        List<Event> feed = eventStorage.getFeedByUserId(user.getId());

        assertThat(feed)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("userId", user.getId())
                .hasFieldOrPropertyWithValue("eventType", EventType.LIKE)
                .hasFieldOrPropertyWithValue("operation", Operation.ADD)
                .hasFieldOrPropertyWithValue("entityId", film.getId());
    }

    @Test
    void testRemoveFriendEvent() {
        User user = createTestUser();

        Event event = new Event();
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(user.getId());
        event.setEventType(EventType.FRIEND);
        event.setOperation(Operation.REMOVE);
        event.setEntityId(10L);

        eventStorage.addEvent(event);

        List<Event> feed = eventStorage.getFeedByUserId(user.getId());

        assertThat(feed)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("userId", user.getId())
                .hasFieldOrPropertyWithValue("eventType", EventType.FRIEND)
                .hasFieldOrPropertyWithValue("operation", Operation.REMOVE)
                .hasFieldOrPropertyWithValue("entityId", 10L);
    }

    @Test
    void testUpdateReviewEvent() {
        User user = createTestUser();

        Event event = new Event();
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(user.getId());
        event.setEventType(EventType.REVIEW);
        event.setOperation(Operation.UPDATE);
        event.setEntityId(50L);

        eventStorage.addEvent(event);

        List<Event> feed = eventStorage.getFeedByUserId(user.getId());

        assertThat(feed)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("userId", user.getId())
                .hasFieldOrPropertyWithValue("eventType", EventType.REVIEW)
                .hasFieldOrPropertyWithValue("operation", Operation.UPDATE)
                .hasFieldOrPropertyWithValue("entityId", 50L);
    }

    @Test
    void testGetFeedReturnsDifferentEvents() {
        User user = createTestUser();
        Film film = createTestFilm();

        Event event1 = new Event();
        event1.setTimestamp(System.currentTimeMillis());
        event1.setUserId(user.getId());
        event1.setEventType(EventType.LIKE);
        event1.setOperation(Operation.ADD);
        event1.setEntityId(film.getId());
        eventStorage.addEvent(event1);

        Event event2 = new Event();
        event2.setTimestamp(System.currentTimeMillis() + 10);
        event2.setUserId(user.getId());
        event2.setEventType(EventType.FRIEND);
        event2.setOperation(Operation.ADD);
        event2.setEntityId(10L);
        eventStorage.addEvent(event2);

        Event event3 = new Event();
        event3.setTimestamp(System.currentTimeMillis() + 20);
        event3.setUserId(user.getId());
        event3.setEventType(EventType.FRIEND);
        event3.setOperation(Operation.REMOVE);
        event3.setEntityId(10L);
        eventStorage.addEvent(event3);

        List<Event> feed = eventStorage.getFeedByUserId(user.getId());

        assertThat(feed).hasSize(3);

        assertThat(feed.get(0))
                .hasFieldOrPropertyWithValue("eventType", EventType.LIKE)
                .hasFieldOrPropertyWithValue("operation", Operation.ADD)
                .hasFieldOrPropertyWithValue("entityId", film.getId());

        assertThat(feed.get(1))
                .hasFieldOrPropertyWithValue("eventType", EventType.FRIEND)
                .hasFieldOrPropertyWithValue("operation", Operation.ADD)
                .hasFieldOrPropertyWithValue("entityId", 10L);

        assertThat(feed.get(2))
                .hasFieldOrPropertyWithValue("eventType", EventType.FRIEND)
                .hasFieldOrPropertyWithValue("operation", Operation.REMOVE)
                .hasFieldOrPropertyWithValue("entityId", 10L);
    }

    private User createTestUser() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail(System.currentTimeMillis() + "bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));
        userStorage.create(user);
        return user;
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        filmStorage.create(film);
        return film;
    }
}