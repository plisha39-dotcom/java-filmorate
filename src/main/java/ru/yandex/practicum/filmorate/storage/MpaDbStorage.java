package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Mpa> rowMapper = ((rs, rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("name"));

        return mpa;
    });

    public MpaDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Collection<Mpa> findAll() {
        return jdbc.query("select mpa_id, name from mpa order by mpa_id",rowMapper);
    }

    @Override
    public Optional<Mpa> findById(int id) {
        String query = "select mpa_id, name from mpa where mpa_id = ?";
        try {
            Mpa mpa = jdbc.queryForObject(query, rowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
