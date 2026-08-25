package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Film> rowMapper = ((rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));

        film.setMpa(mpa);

        return film;
    });
    private final RowMapper<Genre> gRowMapper = ((rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getInt("genre_id"));
        genre.setName(rs.getString("name"));

        return genre;
    });

    public FilmDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Collection<Film> findAll() {
        String query = """
                select f.film_id,
                        f.name AS film_name,
                        f.description,
                        f.duration,
                        f.release_date,
                        m.mpa_id,
                        m.name AS mpa_name
                from films f
                left join mpa m on f.mpa_id = m.mpa_id
                """;
        Collection<Film> films = jdbc.query(query, rowMapper);
        for (Film film : films) {
            film.setGenres(findGenresByFilmId(film.getId()));
            film.setLikes(findLikesByFilmId(film.getId()));
        }
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String query = """
                select f.film_id,
                        f.name AS film_name,
                        f.description,
                        f.duration,
                        f.release_date,
                        m.mpa_id,
                        m.name AS mpa_name
                from films f
                left join mpa m on f.mpa_id = m.mpa_id
                where f.film_id = ?
                """;
        try {
            Film film = jdbc.queryForObject(query, rowMapper, id);
            film.setGenres(findGenresByFilmId(id));
            film.setLikes(findLikesByFilmId(id));
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film film) {
        String query = "insert into films(name, description, release_date, duration, mpa_id) values (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4, film.getDuration());
            ps.setObject(5, film.getMpa().getId());

            return ps;
        }, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            film.setId(id);
            saveGenres(id, film.getGenres());
            saveLikes(id, film.getLikes());
            return film;
        } else {
            throw new RuntimeException("Не удалось получить id созданного фильма");
        }
    }

    @Override
    public Film update(Film film) {
        String query = "update films set name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? where film_id = ?";
        int rowsUpdate = jdbc.update
                (query, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        if (rowsUpdate == 0) {
            throw new NotFoundException("Не удалось обновить данные");
        }
        deleteGenresByFilmId(film.getId());
        saveGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public void delete(Long id) {
        deleteGenresByFilmId(id);
        deleteLikesByFilmId(id);
        String query = "delete from films where film_id = ?";
        jdbc.update(query, id);
    }

    private Set<Genre> findGenresByFilmId(Long filmId) {
        String query = "select g.genre_id, g.name from genres g join film_genres fg on fg.genre_id = g.genre_id where fg.film_id = ?";
        List<Genre> genreList = jdbc.query(query, gRowMapper, filmId);
        return new HashSet<>(genreList);
    }

    private void saveGenres(Long filmId, Set<Genre> genres) {
        String query = "insert into film_genres(film_id, genre_id) values(?, ?)";
        for (Genre genre : genres) {
            jdbc.update(query, filmId, genre.getId());
        }
    }

    private void deleteGenresByFilmId(Long filmId) {
        String query = "delete from film_genres where film_id = ?";
        jdbc.update(query, filmId);
    }

    private Set<Long> findLikesByFilmId(Long filmId) {
        String query = "select user_id from film_likes where film_id = ?";
        List<Long> listLikes = jdbc.query(query, (rs, rowNum) -> rs.getLong("user_id"), filmId);
        return new HashSet<>(listLikes);
    }

    private void saveLikes(Long filmId, Set<Long> likes) {
        String query = "insert into film_likes(film_id, user_id) values(?, ?)";
        for (Long like : likes) {
            jdbc.update(query, filmId, like);
        }
    }

    private void deleteLikesByFilmId(Long filmId) {
        String query = "delete from film_likes where film_id = ?";
        jdbc.update(query, filmId);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String query = "insert into film_likes(film_id, user_id) values(?, ?)";
        jdbc.update(query, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String query = "delete from film_likes where film_id = ? and user_id = ?";
        jdbc.update(query, filmId, userId);
    }
}
