package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<User> mapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        return user;
    };

    public UserDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Collection<User> findAll() {
        return jdbc.query("select * from users", mapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        String query = "select * from users where user_id = ?";
        try {
            User user = jdbc.queryForObject(query, mapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        String query = "insert into users(email, login, name, birthday) values (?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3, user.getName());
            ps.setObject(4, user.getBirthday());

            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            user.setId(id);
            return user;
        } else {
            throw new RuntimeException("Не удалось получить id созданного пользователя");
        }
    }

    @Override
    public User update(User user) {
        String query = "update users set email = ?, login = ?, name = ?, birthday = ? where user_id = ?";
        int rowsUpdated = jdbc.update(query, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        if (rowsUpdated == 0) {
            throw new RuntimeException("Не удалось обновить данные");
        }
        return user;
    }

    @Override
    public void delete(Long id) {
        String query = "delete from users where user_id = ?";
        jdbc.update(query, id);
    }
}
