package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class InMemoryFilmStorageTest {
    private FilmStorage filmStorage;
    private UserStorage userStorage;
    
    @BeforeEach
    void setUp() {
        filmStorage = new InMemoryFilmStorage();
        userStorage = new InMemoryUserStorage();
    }
    
    @Test
    void testUpdateFilmPreservesLikes() {
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
        
        filmStorage.create(film);
        
        Film savedFilm = filmStorage.findById(film.getId()).orElseThrow();
        
        savedFilm.getLikes().add(user.getId());
        
        Film updateFilm = new Film();
        updateFilm.setId(film.getId());
        updateFilm.setName("Новый фильм");
        updateFilm.setDescription(film.getDescription());
        updateFilm.setReleaseDate(film.getReleaseDate());
        updateFilm.setDuration(film.getDuration());
        
        filmStorage.update(updateFilm);
        
        Film actualFilm = filmStorage.findById(film.getId()).orElseThrow();
        
        Assertions.assertEquals(
                "Новый фильм",
                actualFilm.getName(),
                "Название фильма должно обновиться"
        );
        
        Assertions.assertTrue(
                actualFilm.getLikes().contains(user.getId()),
                "После обновления должен сохраниться ID пользователя, поставившего лайк"
        );
        
        Assertions.assertEquals(
                1,
                actualFilm.getLikes().size(),
                "Количество лайков должно остаться равным 1"
        );
    }
    
    @Test
    void testCreateDoesNotReuseDeletedFilmId() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        
        filmStorage.create(film);
        
        Film film1 = new Film();
        film1.setName("Начало");
        film1.setDescription("Новый фильм");
        film1.setReleaseDate(LocalDate.of(2000, 11, 11));
        film1.setDuration(150);
        
        filmStorage.create(film1);
        
        Film film2 = new Film();
        film2.setName("Новый фильм2");
        film2.setDescription("Новый фильм 2");
        film2.setReleaseDate(LocalDate.of(1999, 2, 2));
        film2.setDuration(140);
        
        filmStorage.create(film2);
        
        Assertions.assertEquals(3L, film2.getId(), "Третий фильм должен получить ID 3");
        
        filmStorage.delete(film2.getId());
        
        Film film3 = new Film();
        film3.setName("Новый фильм3");
        film3.setDescription("Новый фильм 3");
        film3.setReleaseDate(LocalDate.of(1999, 2, 2));
        film3.setDuration(140);
        
        filmStorage.create(film3);
        
        Assertions.assertEquals(
                4L,
                film3.getId(),
                "После удаления фильма следующий ID должен быть равен 4"
        );
        
        Assertions.assertNotEquals(
                film2.getId(),
                film3.getId(),
                "Удалённый ID не должен использоваться повторно"
        );
    }
}
