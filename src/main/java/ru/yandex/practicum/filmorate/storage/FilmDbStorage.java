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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        Integer mpaId = rs.getObject("mpa_id", Integer.class);
        if (mpaId != null) {
            Mpa mpa = new Mpa();
            mpa.setName(rs.getString("mpa_name"));
            mpa.setId(mpaId);
            film.setMpa(mpa);
        }
        return film;
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
        populateLikesAndGenres(films);
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
            film.setGenres(findGenresByFilmIds(List.of(id)).getOrDefault(id, new HashSet<>()));
            film.setLikes(findLikesByFilmIds(List.of(id)).getOrDefault(id, new HashSet<>()));
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film film) {
        String query = "insert into films(name, description, release_date, duration, mpa_id) values (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        Integer mpaId;
        if (film.getMpa() == null) {
            mpaId = null;
        } else {
            mpaId = film.getMpa().getId();
        }
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4, film.getDuration());
            ps.setObject(5, mpaId);

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
        Integer mpaId;
        if (film.getMpa() == null) {
            mpaId = null;
        } else {
            mpaId = film.getMpa().getId();
        }
        int rowsUpdate = jdbc.update(query, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), mpaId, film.getId());
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

    private Map<Long, Set<Genre>> findGenresByFilmIds(Collection<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return new HashMap<Long, Set<Genre>>();
        }
        String placeholder = Stream.generate(() -> "?")
                                   .limit(filmIds.size())
                                   .collect(Collectors.joining(", "));
        String query = "select g.genre_id, g.name, fg.film_id from genres g join film_genres fg on fg.genre_id = g.genre_id where fg.film_id in (" + placeholder + ")";
        HashMap<Long, Set<Genre>> genresByFilmId = new HashMap<>();
        jdbc.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            genresByFilmId.computeIfAbsent(filmId, id -> new HashSet<>())
                          .add(genre);
        }, filmIds.toArray());
        return genresByFilmId;
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

    private Map<Long, Set<Long>> findLikesByFilmIds(Collection<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return new HashMap<Long, Set<Long>>();
        }
        String placeholder = Stream.generate(() -> "?")
                                   .limit(filmIds.size())
                                   .collect(Collectors.joining(", "));
        String query = "select user_id, film_id from film_likes where film_id in (" + placeholder + ")";
        HashMap<Long, Set<Long>> likesByFilmId = new HashMap<>();
        jdbc.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            likesByFilmId.computeIfAbsent(filmId, id -> new HashSet<>())
                         .add(userId);
        }, filmIds.toArray());
        return likesByFilmId;
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

    @Override
    public List<Film> commonFilms(Long userId, Long friendId) {
        String query = """
                select f.film_id,
                       f.name AS film_name,
                       f.description,
                       f.duration,
                       f.release_date,
                       m.mpa_id,
                       m.name AS mpa_name
                from films f
                join film_likes fl
                    on f.film_id = fl.film_id
                left join mpa m on f.mpa_id = m.mpa_id
                where f.film_id IN (
                    select film_id
                    from film_likes
                    where user_id = ? or user_id = ?
                    group by film_id
                    having count(film_id) = 2
                )
                group by f.film_id
                order by count(fl.user_id) DESC
                """;
        List<Film> films = jdbc.query(query, rowMapper, userId, friendId);
        populateLikesAndGenres(films);
        return films;
    }

    private void populateLikesAndGenres(Collection<Film> films) {
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        Map<Long, Set<Genre>> genresByFilmId = findGenresByFilmIds(filmIds);
        Map<Long, Set<Long>> likesByFilmId = findLikesByFilmIds(filmIds);
        for (Film film : films) {
            Long filmId = film.getId();
            Set<Genre> genres = genresByFilmId.getOrDefault(filmId, new HashSet<>());
            film.setGenres(genres);
            Set<Long> likes = likesByFilmId.getOrDefault(filmId, new HashSet<>());
            film.setLikes(likes);
        }
    }
}
