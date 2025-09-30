package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * Глобальный обработчик исключений для приложения ShareIt.
 * Перехватывает бизнес-исключения (404, 409) и ошибки валидации Spring (400).
 */
@RestControllerAdvice
public class ErrorHandler {

    /**
     * Обрабатывает исключения типа NotFoundException (404 Not Found).
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 404.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        return new ErrorResponse("Объект не найден", e.getMessage());
    }

    /**
     * Обрабатывает исключения типа ValidationException (409 Conflict).
     * Используется для конфликтов уникальности (email) или нарушений прав доступа.
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 409.
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public ErrorResponse handleValidationException(final ValidationException e) {
        return new ErrorResponse("Нарушение бизнес-правил", e.getMessage());
    }

    // --- Обработчики HTTP 400 Bad Request ---

    /**
     * Обрабатывает ошибки валидации DTO, помеченных @Valid (HTTP 400 Bad Request).
     * Перехватывает MethodArgumentNotValidException, которое выбрасывается при
     * нарушении аннотаций @NotBlank, @NotNull и т.д. в DTO.
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        String defaultMessage = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return new ErrorResponse("Ошибка валидации DTO", defaultMessage);
    }

    /**
     * Обрабатывает исключения, когда обязательный заголовок (например, X-Sharer-User-Id) отсутствует (HTTP 400).
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 400.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorResponse handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        return new ErrorResponse("Отсутствует обязательный заголовок", "Заголовок " + e.getHeaderName() + " обязателен.");
    }

    /**
     * Универсальный обработчик для всех необработанных RuntimeException (HTTP 500).
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 500.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public ErrorResponse handleThrowable(final Throwable e) {
        return new ErrorResponse("Непредвиденная ошибка сервера", e.getMessage());
    }
}