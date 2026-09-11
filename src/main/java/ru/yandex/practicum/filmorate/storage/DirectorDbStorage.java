package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Repository
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Director> rowMapper = (rs, rowNum) -> {
        Director director = new Director();
        director.setId(rs.getInt("director_id"));
        director.setName(rs.getString("name"));
        return director;
    };

    public DirectorDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Collection<Director> findAll() {
        return jdbc.query("select director_id, name from directors order by director_id", rowMapper);
    }

    @Override
    public Optional<Director> findById(Integer id) {
        String query = "select director_id, name from directors where director_id = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(query, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Director create(Director director) {
        String query = "INSERT INTO directors (name) VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(keyHolder.getKeyAs(Integer.class));
        return director;
    }

    @Override
    public Director update(Director director) {
        String query = "UPDATE directors SET name = ? WHERE director_id = ?";
        int rows = jdbc.update(query, director.getName(), director.getId());
        if (rows == 0) {
            throw new NotFoundException("Режиссер с id " + director.getId() + " не найден");
        }
        return director;
    }

    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM directors WHERE director_id = ?";
        int rows = jdbc.update(query, id);
        if (rows == 0) {
            throw new NotFoundException("Режиссер с id " + id + " не найден");
        }
    }
}