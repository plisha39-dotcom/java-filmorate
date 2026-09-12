package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final DirectorStorage directorStorage;
    private final EventService eventService;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("directorDbStorage") DirectorStorage directorStorage,
                       EventService eventService, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.directorStorage = directorStorage;
        this.eventService = eventService;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public void addLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        filmStorage.addLike(filmId, userId);
        eventService.createEvent(userId, EventType.LIKE, Operation.ADD, filmId);
        log.info("Пользователь userId={} поставил лайк фильму filmId={}", userId, film.getId());
    }

    public void removeLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        filmStorage.removeLike(filmId, userId);
        eventService.createEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
        log.info("Пользователь userId={} удалил лайк фильму filmId={}", userId, film.getId());
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        if (count < 0) {
            log.warn("Ошибка валидации: count={} не может быть отрицательным", count);
            throw new ValidationException("Количество фильмов не может быть отрицательным");
        }
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.findById(filmId)
                          .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    private void checkUserExists(Long userId) {
        userStorage.findById(userId)
                   .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    public void deleteFilm(Long filmId) {
        getFilmById(filmId);
        filmStorage.delete(filmId);
        log.info("Фильм с id {} успешно удален", filmId);
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Параметр 'query' не может быть пустым");
        }

        String normalizedBy = by.toLowerCase().replaceAll("\\s+", "");
        if (!normalizedBy.equals("title") &&
                !normalizedBy.equals("director") &&
                !normalizedBy.equals("title,director") &&
                !normalizedBy.equals("director,title")) {
            throw new ValidationException("Параметр 'by' должен быть 'title', 'director' или 'title,director'");
        }

        return filmStorage.searchFilms(query, normalizedBy);
    }

    public List<Film> getFilmsByDirector(Integer directorId, String sortBy) {
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes'");
        }
        directorStorage.findById(directorId)
                       .orElseThrow(() -> new NotFoundException("Режиссер с id " + directorId + " не найден"));
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilm(film);
        return filmStorage.update(film);
    }

    private void validateFilm(Film film) {
        validateReleaseDate(film);
        checkMpaExists(film);
        checkGenresExists(film);
        checkDirectorsExists(film);
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: некорректная дата релиза");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void checkMpaExists(Film film) {
        Mpa mpa = film.getMpa();
        if (mpa == null) {
            return;
        }
        int mpaId = mpa.getId();
        if (mpaStorage.findById(mpaId).isEmpty()) {
            throw new NotFoundException("Рейтинг с id " + mpaId + " не найден");
        }
    }

    private void checkGenresExists(Film film) {
        Set<Genre> genres = film.getGenres();
        if (genres == null || genres.isEmpty()) {
            return;
        }
        for (Genre genre : genres) {
            int genreId = genre.getId();
            if (genreStorage.findById(genreId).isEmpty()) {
                throw new NotFoundException("Жанр с id " + genreId + " не найден");
            }
        }
    }

    private void checkDirectorsExists(Film film) {
        Set<Director> directors = film.getDirectors();
        if (directors == null || directors.isEmpty()) {
            return;
        }
        for (Director director : directors) {
            if (directorStorage.findById(director.getId()).isEmpty()) {
                throw new NotFoundException("Режиссер с id " + director.getId() + " не найден");
            }
        }
    }
}
