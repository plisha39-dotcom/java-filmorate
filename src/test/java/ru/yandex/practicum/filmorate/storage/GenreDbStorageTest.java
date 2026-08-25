package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTest {
    private final GenreDbStorage storage;
    
    @Test
    void testFindGenreById() {
        Optional<Genre> optional = storage.findById(1);
        
        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(foundGenre ->
                        assertThat(foundGenre).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Комедия"));
    }
    
    @Test
    void testFindAllGenres() {
        Collection<Genre> collection = storage.findAll();
        
        assertThat(collection).hasSize(6);
        assertThat(collection).
                extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
    }
}
