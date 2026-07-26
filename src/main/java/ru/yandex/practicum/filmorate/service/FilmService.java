package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public FilmService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public void addLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        film.getLikes().add(userId);
        filmStorage.update(film);
        log.info("Пользователь userId={} поставил лайк фильму filmId={}", userId, film.getId());
    }

    public void removeLike(Long filmId, Long userId) {
        checkUserExists(userId);
        Film film = getFilmById(filmId);
        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.info("Пользователь userId={} удалил лайк фильму filmId={}", userId, film.getId());
    }

    public Collection<Film> getPopularFilms(int count) {
        Collection<Film> popularFilms = filmStorage.findAll()
                .stream()
                .sorted((film1, film2) -> Integer.compare(
                        film2.getLikes().size(),
                        film1.getLikes().size()
                ))
                .limit(count)
                .toList();

        log.debug(
                "Получен список популярных фильмов: requestedCount={}, resultCount={}",
                count,
                popularFilms.size()
        );

        return popularFilms;
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
