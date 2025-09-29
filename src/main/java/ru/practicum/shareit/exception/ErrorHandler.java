package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений для приложения.
 * Перехватывает бизнес-исключения и возвращает соответствующие HTTP-статусы.
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    /**
     * Обрабатывает исключения типа NotFoundException (404 Not Found).
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(final NotFoundException e) {
        log.error("Обработано исключение: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Пользователь не найден");
    }

    /**
     * Обрабатывает исключения типа ValidationException (409 Conflict).
     * Используется для конфликтов уникальности, например, при дублировании email.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<String> handleValidationException(final ValidationException e) {
        log.error("Обработано исключение: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Нарушение уникальности: email уже существует");
    }
}