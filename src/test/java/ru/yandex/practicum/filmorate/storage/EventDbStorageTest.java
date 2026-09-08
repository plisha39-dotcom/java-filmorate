package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Operation;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ EventDbStorage.class,
        UserDbStorage.class,
        FilmDbStorage.class,
        MpaDbStorage.class,     // Добавили реализацию MPA
        GenreDbStorage.class    })
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventDbStorageTest {

    private final EventDbStorage eventStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Test
    void testAddFriendEvent() {
        User user = new User();
        user.setName("Иван");
        user.setLogin("ivan");
        user.setEmail("ivan@yandex.ru");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userStorage.create(user);

        Event event = new Event();
        event.setTimestamp(System.currentTimeMillis());
        event.setUserId(user.getId());
        event.setEventType(EventType.FRIEND);
        event.setOperation(Operation.ADD);
        event.setEntityId(10L);

        eventStorage.addEvent(event);

        List<Event> feed = eventStorage.getFeedByUserId(user.getId());

        assertThat(feed)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("userId", user.getId())
                .hasFieldOrPropertyWithValue("eventType", EventType.FRIEND)
                .hasFieldOrPropertyWithValue("operation", Operation.ADD)
                .hasFieldOrPropertyWithValue("entityId", 10L);
    }

    @Test
    void testAddLikeEvent() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

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
}