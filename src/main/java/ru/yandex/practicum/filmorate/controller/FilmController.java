package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> allFilms() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм добавлен: id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm == null) {
            log.warn("Ошибка валидации: пустой фильм");
            throw new ValidationException("Фильм не может быть пустым");
        }
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации: отсутствует Id фильма");
            throw new ValidationException("Id должен быть указан!");
        }
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            log.warn("Ошибка обновления: фильм с id {} не найден", newFilm.getId());
            throw new ValidationException("Фильм с таким id не найден");
        }
        validateFilm(newFilm);
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм обновлен: id={}", newFilm.getId());
        return newFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    private void validateFilm(Film film) {
        if (film == null) {
            log.warn("Ошибка валидации: пустой фильм");
            throw new ValidationException("Фильм не может быть пустым");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: название пустое");
            throw new ValidationException("Название не может быть пустым!");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации: в описании превышен лимит символов");
            throw new ValidationException("Длина описания не должна превышать 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: некорректная дата релиза");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Ошибка валидации: некорректная продолжительность фильма");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
