package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> rowMapper = ((rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));

        return genre;
    });

    public GenreDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query("select genre_id, name from genres order by genre_id", rowMapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        String query = "select genre_id, name from genres where genre_id = ?";
        try {
            Genre genre = jdbc.queryForObject(query, rowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
