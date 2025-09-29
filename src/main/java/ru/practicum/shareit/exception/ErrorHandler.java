package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

        /**
     * Обрабатывает MethodArgumentNotValidException, которое выбрасывается, когда
     * @Valid или @Validated не проходят проверку.
     * Возвращает HTTP 400 Bad Request с деталями ошибок валидации.
     *
     * @param e Перехваченное исключение.
     * @return ResponseEntity с HTTP-статусом 400 и структурой ошибок.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Collection<String>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        log.warn("Получено исключение MethodArgumentNotValidException");

        // 1. Создаем карту для сбора всех ошибок валидации
        Map<String, String> errors = new HashMap<>();

        // 2. Итерируемся по всем ошибкам, собранным BindingResult
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()) // Ключ - имя поля, Значение - сообщение об ошибке
        );

        log.debug("Собранные ошибки валидации: {}", errors);

        // 3. Возвращаем HTTP 400 Bad Request и список ошибок
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400
                .body(errors.values());
    }
}