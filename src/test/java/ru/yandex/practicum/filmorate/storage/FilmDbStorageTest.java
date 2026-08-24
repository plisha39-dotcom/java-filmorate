package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void testCreateFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

        assertThat(film.getId()).isNotNull();

        assertThat(filmStorage.findById(film.getId())).isPresent();
    }

    @Test
    void testFindFilmById() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

        Optional<Film> optional = filmStorage.findById(film.getId());

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(foundFilm -> {
                    assertThat(foundFilm)
                            .hasFieldOrPropertyWithValue("id", film.getId())
                            .hasFieldOrPropertyWithValue("name", film.getName())
                            .hasFieldOrPropertyWithValue("description", film.getDescription())
                            .hasFieldOrPropertyWithValue("releaseDate", film.getReleaseDate())
                            .hasFieldOrPropertyWithValue("duration", film.getDuration());

                    assertThat(foundFilm.getMpa().getId())
                            .isEqualTo(film.getMpa().getId());
                });
    }

    @Test
    void testFindAllFilms() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

        Film film1 = new Film();
        film1.setName("Начало");
        film1.setDescription("Новый фильм");
        film1.setReleaseDate(LocalDate.of(2000, 11, 11));
        film1.setDuration(150);
        Mpa mpa1 = new Mpa();
        mpa1.setId(1);
        film1.setMpa(mpa1);

        filmStorage.create(film1);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
        assertThat(films)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(film.getId(), film1.getId());
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(film.getId());
        updateFilm.setName("Новый фильм");
        updateFilm.setDescription("Новое описание");
        updateFilm.setReleaseDate(LocalDate.of(2000, 11, 11));
        updateFilm.setDuration(20);
        Mpa mpa1 = new Mpa();
        mpa1.setId(2);
        updateFilm.setMpa(mpa1);

        filmStorage.update(updateFilm);

        Optional<Film> optionalFilm = filmStorage.findById(film.getId());

        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(foundFilm -> {
                    assertThat(foundFilm).hasFieldOrPropertyWithValue("id", updateFilm.getId())
                            .hasFieldOrPropertyWithValue("name", updateFilm.getName())
                            .hasFieldOrPropertyWithValue("description", updateFilm.getDescription())
                            .hasFieldOrPropertyWithValue("releaseDate", updateFilm.getReleaseDate())
                            .hasFieldOrPropertyWithValue("duration", updateFilm.getDuration());
                    assertThat(foundFilm.getMpa().getId())
                            .isEqualTo(updateFilm.getMpa().getId());
                });
    }

    @Test
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

        Long filmId = film.getId();

        filmStorage.delete(filmId);
        Optional<Film> optionalFilm = filmStorage.findById(filmId);

        assertThat(optionalFilm).isEmpty();
    }

    @Test
    void testAddLike() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        filmStorage.addLike(film.getId(), user.getId());
        Optional<Film> optional = filmStorage.findById(film.getId());

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(foundFilm ->
                    assertThat(foundFilm.getLikes()).contains(user.getId()));
    }

    @Test
    void testRemoveLike() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        filmStorage.create(film);

        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));

        userStorage.create(user);

        filmStorage.addLike(film.getId(), user.getId());

        filmStorage.removeLike(film.getId(), user.getId());
        Optional<Film> optional = filmStorage.findById(film.getId());

        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(foundFilm ->
                        assertThat(foundFilm.getLikes()).doesNotContain(user.getId()));
    }
}
