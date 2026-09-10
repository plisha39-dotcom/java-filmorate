package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({ReviewDbStorage.class, UserDbStorage.class, FilmDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ReviewDbStorageTest {
    private final ReviewDbStorage reviewStorage;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Test
    void testCreateAndFindReview() {
        User user = createTestUser();
        Film film = createTestFilm();

        Review review = new Review();
        review.setContent("Отличный фильм");
        review.setIsPositive(true);
        review.setUserId(user.getId());
        review.setFilmId(film.getId());

        reviewStorage.create(review);
        Optional<Review> optionalReview = reviewStorage.findById(review.getReviewId());

        assertThat(optionalReview)
                .isPresent()
                .hasValueSatisfying(foundReview ->
                        assertThat(foundReview).hasFieldOrPropertyWithValue("reviewId", review.getReviewId())
                                .hasFieldOrPropertyWithValue("content", "Отличный фильм")
                                .hasFieldOrPropertyWithValue("useful", 0)
                );
    }

    @Test
    void testUpdateReview() {
        User user = createTestUser();
        Film film = createTestFilm();

        Review review = new Review();
        review.setContent("Сначала понравился");
        review.setIsPositive(true);
        review.setUserId(user.getId());
        review.setFilmId(film.getId());
        reviewStorage.create(review);

        Review updatedReview = new Review();
        updatedReview.setReviewId(review.getReviewId());
        updatedReview.setContent("Передумал, фильм плохой");
        updatedReview.setIsPositive(false);

        reviewStorage.update(updatedReview);
        Optional<Review> optionalReview = reviewStorage.findById(review.getReviewId());

        assertThat(optionalReview)
                .isPresent()
                .hasValueSatisfying(foundReview -> assertThat(foundReview)
                        .hasFieldOrPropertyWithValue("content", "Передумал, фильм плохой")
                        .hasFieldOrPropertyWithValue("isPositive", false)
                );
    }

    @Test
    void testLikesAndDislikesChangeUsefulField() {
        User author = createTestUser();
        User userWhoLikes = createTestUser();
        Film film = createTestFilm();

        Review review = new Review();
        review.setContent("Полезный отзыв");
        review.setIsPositive(true);
        review.setUserId(author.getId());
        review.setFilmId(film.getId());
        reviewStorage.create(review);

        reviewStorage.addLikeDislike(review.getReviewId(), userWhoLikes.getId(), true);
        Optional<Review> withLike = reviewStorage.findById(review.getReviewId());
        assertThat(withLike).isPresent().hasValueSatisfying(r -> assertThat(r.getUseful()).isEqualTo(1));

        reviewStorage.addLikeDislike(review.getReviewId(), userWhoLikes.getId(), false);
        Optional<Review> withDislike = reviewStorage.findById(review.getReviewId());
        assertThat(withDislike).isPresent().hasValueSatisfying(r -> assertThat(r.getUseful()).isEqualTo(-1));
    }

    private User createTestUser() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail(System.currentTimeMillis() + "bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));
        userStorage.create(user);
        return user;
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Фильм для отзыва");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(120);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
        filmStorage.create(film);
        return film;
    }
}