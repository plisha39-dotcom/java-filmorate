package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final DirectorStorage directorStorage;

    public FilmService(@Qualifier("userDbStorage") UserStorage userStorage, @Qualifier("filmDbStorage")
    FilmStorage filmStorage, @Qualifier("directorDbStorage")DirectorStorage directorStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.directorStorage=directorStorage;
    }

    public void addLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        filmStorage.addLike(filmId, userId);
        log.info("Пользователь userId={} поставил лайк фильму filmId={}", userId, film.getId());
    }

    public void removeLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        filmStorage.removeLike(filmId, userId);
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

    private Film getFilmById(Long filmId) {
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
}
