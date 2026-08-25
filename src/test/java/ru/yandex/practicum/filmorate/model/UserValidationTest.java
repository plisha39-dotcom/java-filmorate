package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

public class UserValidationTest {
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        validator = validator = Validation
                .buildDefaultValidatorFactory()
                .getValidator();
    }
    
    @Test
    void testInvalidEmailProducesValidationViolation() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("boryandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        Assertions.assertFalse(violations.isEmpty(),
                "Для некорректного email должно найтись нарушение валидации");
        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("email")),
                "Нарушение валидации должно относиться к email");
    }
    
    @Test
    void testBlankEmailProducesNotBlankViolation() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("      ");
        user.setBirthday(LocalDate.of(1999, 1, 15));
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath().toString().equals("email")
                                        && violation.getConstraintDescriptor()
                                        .getAnnotation()
                                        .annotationType()
                                        == NotBlank.class),
                "Пустой email должен нарушать ограничение @NotBlank"
        );
    }
    
    @Test
    void testLoginWithSpacesProducesPatternViolation() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("bad login");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation ->
                                violation.getPropertyPath().toString().equals("login")
                                        && violation.getConstraintDescriptor()
                                        .getAnnotation()
                                        .annotationType()
                                        == Pattern.class),
                "Логин с пробелами должен нарушать ограничение @Pattern"
        );
    }
    
    @Test
    void testBlankLoginProducesNotBlankViolation() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("       ");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.of(1999, 1, 15));
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("login")
                                && violation.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                == NotBlank.class),
                "Логин с пробелами должен нарушать ограничения @NotBlank");
    }
    
    @Test
    void testFutureBirthdayProducesPastOrPresentViolation() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(LocalDate.now().plusDays(1));
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("birthday")
                                && violation.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                == PastOrPresent.class),
                "Пользователь с датой рождения в будущем должен нарушать ограничение @PastOrPresent");
    }
    
    @Test
    void testNullBirthdayProducesNotNullViolation() {
        User user = new User();
        user.setName("Борис");
        user.setLogin("BOR");
        user.setEmail("bor@yandex.ru");
        user.setBirthday(null);
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        Assertions.assertTrue(violations.stream()
                        .anyMatch(violation -> violation.getPropertyPath().toString().equals("birthday")
                                && violation.getConstraintDescriptor()
                                .getAnnotation()
                                .annotationType()
                                == NotNull.class),
                "Пользователь с пустой датой рождения должен нарушать ограничение @NotNull");
    }
}
