package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(@Qualifier("filmDbStorage")FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> allFilms() {
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @GetMapping("/popular")
    public Collection<Film> popularFilms(
            @RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateReleaseDate(film);
        return filmStorage.create(film);
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
        validateReleaseDate(newFilm);
        return filmStorage.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable Long id,
                         @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: некорректная дата релиза");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
