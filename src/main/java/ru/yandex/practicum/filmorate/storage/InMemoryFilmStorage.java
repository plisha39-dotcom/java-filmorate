package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @Override
    public Collection<Film> findAll() {
        return List.copyOf(films.values());
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());
        if (oldFilm == null) {
            log.warn("Ошибка обновления: фильм с id {} не найден", film.getId());
            throw new NotFoundException("Фильм с таким id не найден");
        }
        film.setLikes(new HashSet<>(oldFilm.getLikes()));
        films.put(film.getId(), film);
        log.info("Фильм обновлен: id={}", oldFilm.getId());
        return film;
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм добавлен: id={}", film.getId());
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void delete(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Ошибка удаления: фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с таким id не найден");
        }
        films.remove(id);
        log.info("Фильм удален: id={}", id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            log.warn("Ошибка добавления лайка: фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с таким id не найден");
        }
        film.getLikes().add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = films.get(filmId);
        if (film == null) {
            log.warn("Ошибка удаления: фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с таким id не найден");
        }
        film.getLikes().remove(userId);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return findAll()
                .stream()
                .filter(film -> film.getLikes().contains(userId) && film.getLikes().contains(friendId))
                .sorted((film1, film2) -> Integer.compare(
                        film2.getLikes().size(),
                        film1.getLikes().size()
                ))
                .toList();
    }

    private long getNextId() {
        return ++currentId;
    }

    public void removeLikesByUser(Long userId) {
        films.values().forEach(film -> film.getLikes().remove(userId));
    }
}
