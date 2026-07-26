package ru.yandex.practicum.filmorate.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException exception) {
        ErrorResponse response = new ErrorResponse(exception.getMessage());
        log.warn("Ошибка валидации: {}", exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException exception) {
        ErrorResponse response = new ErrorResponse(exception.getMessage());
        log.warn("Объект не найден: {}", exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        ErrorResponse response = new ErrorResponse(exception.getMessage());
        log.error("Непредвиденная ошибка", exception);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ErrorResponse response = new ErrorResponse(exception.getMessage());
        log.warn("Ошибка валидации"); // тут может сформироваться большой текст, поэтому оставил лог без exception.getMessage().
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
