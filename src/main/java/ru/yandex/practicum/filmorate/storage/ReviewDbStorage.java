package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbc;

    private final RowMapper<Review> mapper = (rs, rowNum) -> {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    };

    public ReviewDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Review create(Review review) {
        String query = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String query = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int rowsUpdate = jdbc.update(query, review.getContent(), review.getIsPositive(), review.getReviewId());
        if (rowsUpdate == 0) {
            throw new NotFoundException("Не удалось обновить данные отзыва с id " + review.getReviewId());
        }
        return findById(review.getReviewId()).get();
    }

    @Override
    public void delete(Long id) {
        String query = "DELETE FROM reviews WHERE review_id = ?";
        jdbc.update(query, id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        String query = "SELECT * FROM reviews WHERE review_id = ?";
        return jdbc.query(query, mapper, id).stream().findFirst();
    }

    @Override
    public List<Review> findReviewsByFilmId(Long filmId, int count) {
        String query;
        if (filmId == null) {
            query = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
            return jdbc.query(query, mapper, count);
        } else {
            query = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            return jdbc.query(query, mapper, filmId, count);
        }
    }

    @Override
    public void addLikeDislike(Long reviewId, Long userId, boolean isLike) {
        jdbc.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
        jdbc.update("INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)", reviewId, userId, isLike);

        updateReviewUsefulField(reviewId);
    }

    @Override
    public void removeLikeDislike(Long reviewId, Long userId, boolean isLike) {
        jdbc.update("DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = ?", reviewId, userId, isLike);
        updateReviewUsefulField(reviewId);
    }

    private void updateReviewUsefulField(Long reviewId) {
        String query = "UPDATE reviews SET useful = (SELECT COALESCE(SUM(CASE WHEN is_like = true THEN 1 ELSE -1 END), 0) " +
                "FROM review_likes WHERE review_id = ?) WHERE review_id = ?";
        jdbc.update(query, reviewId, reviewId);
    }
}
