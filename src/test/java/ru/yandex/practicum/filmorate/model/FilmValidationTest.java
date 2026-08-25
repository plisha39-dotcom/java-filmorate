package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

public class FilmValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }

    @Test
    void testBlankNameProducesNotBlankViolation() {
        Film film = new Film();
        film.setName("    ");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("name")
                                && violation.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                == NotBlank.class),
                "Фильм с пустым названием должен нарушать ограничение @NotBlank");
    }

    @Test
    void testTooLongDescriptionProducesSizeViolation() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("description")
                                && violation.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                == Size.class),
                "Описание длиной больше 200 символов должно нарушать ограничение @Size");
    }

    @Test
    void testNonPositiveDurationProducesPositiveViolation() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("duration")
                                && violation.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                == Positive.class),
                "Филь с продолжительностью <= 0 должен нарушать ограничение @Positive");
    }

    @Test
    void testNullReleaseDateProducesNotNullViolation() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Фантастический фильм");
        film.setReleaseDate(null);
        film.setDuration(169);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("releaseDate")
                                && violation.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType() ==
                                NotNull.class),
                "Фильм с пустой датой релиза должен нарушать ограничение @NotNull");
    }
}
