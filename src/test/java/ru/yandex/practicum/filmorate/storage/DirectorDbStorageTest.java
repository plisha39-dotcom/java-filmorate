package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(DirectorDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDbStorageTest {

    private final DirectorDbStorage directorStorage;

    @Test
    void testCreateDirector() {
        Director director = new Director();
        director.setName("Кристофер Нолан");

        directorStorage.create(director);

        assertThat(director.getId()).isNotNull();
        assertThat(directorStorage.findById(director.getId())).isPresent();
    }

    @Test
    void testFindDirectorById() {
        Director director = new Director();
        director.setName("Дэвид Финчер");
        directorStorage.create(director);

        Optional<Director> found = directorStorage.findById(director.getId());

        assertThat(found).isPresent()
                .hasValueSatisfying(d -> {
                    assertThat(d.getId()).isEqualTo(director.getId());
                    assertThat(d.getName()).isEqualTo("Дэвид Финчер");
                });
    }

    @Test
    void testFindAllDirectors() {
        Director d1 = new Director();
        d1.setName("Режиссер 1");
        directorStorage.create(d1);

        Director d2 = new Director();
        d2.setName("Режиссер 2");
        directorStorage.create(d2);

        Collection<Director> directors = directorStorage.findAll();

        assertThat(directors).hasSize(2);
        assertThat(directors)
                .extracting(Director::getId)
                .containsExactlyInAnyOrder(d1.getId(), d2.getId());
    }

    @Test
    void testUpdateDirector() {
        Director director = new Director();
        director.setName("Старое имя");
        directorStorage.create(director);

        director.setName("Новое имя");
        Director updated = directorStorage.update(director);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(directorStorage.findById(director.getId()).get().getName()).isEqualTo("Новое имя");
    }

    @Test
    void testDeleteDirector() {
        Director director = new Director();
        director.setName("Квентин Тарантино");
        directorStorage.create(director);

        directorStorage.delete(director.getId());

        assertThat(directorStorage.findById(director.getId())).isEmpty();
    }
}