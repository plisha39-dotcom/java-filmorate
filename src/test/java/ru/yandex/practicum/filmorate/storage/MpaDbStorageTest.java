package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {
    private final MpaDbStorage storage;
    
    @Test
    void testFindMpaById() {
        Optional<Mpa> optional = storage.findById(1);
        
        assertThat(optional)
                .isPresent()
                .hasValueSatisfying(foundMpa ->
                        assertThat(foundMpa).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "G"));
    }
    
    @Test
    void testFindAllMpa() {
        Collection<Mpa> collection = storage.findAll();
        
        assertThat(collection).hasSize(5);
        assertThat(collection).
                extracting(Mpa::getId)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5);
    }
}
