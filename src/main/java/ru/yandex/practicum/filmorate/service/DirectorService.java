package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(int id) {
        return directorStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (director.getId() == null) {
            throw new ValidationException("Id режиссера должен быть указан");
        }
        return directorStorage.update(director);
    }

    public void delete(int id) {
        directorStorage.delete(id);
    }
}