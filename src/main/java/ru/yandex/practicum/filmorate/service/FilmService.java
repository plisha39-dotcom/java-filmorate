package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("userDbStorage") UserStorage userStorage, @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
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
}
