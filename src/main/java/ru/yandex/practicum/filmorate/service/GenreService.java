package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<Genre> getAllGenres() {
        return genreStorage.findAll();
    }

    public Genre getGenreById(int genreId) {
        return genreStorage.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id " + genreId + " не найден"));
    }
}
