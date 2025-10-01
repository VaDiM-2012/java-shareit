package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ErrorHandler {

    private ErrorResponse createErrorResponse(String error, Throwable e) {
        return new ErrorResponse(error, e.getMessage());
    }

    /**
     * Обрабатывает исключения типа NotFoundException (404 Not Found).
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 404.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.error(e.getMessage(), e);
        return createErrorResponse("Объект не найден", e);
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
        log.error(e.getMessage(), e);
        return createErrorResponse("Нарушение бизнес-правил", e);
    }

    /**
     * Обрабатывает исключения типа DuplicateEmailException (409 Conflict).
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 409.
     */
    @ExceptionHandler(InvalidUserEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public ErrorResponse handleDuplicateEmailException(final InvalidUserEmailException e) {
        log.error(e.getMessage(), e);
        return createErrorResponse("Конфликт уникальности", e);
    }

    /**
     * Обрабатывает исключения типа OwnerMismatchException (409 Conflict).
     *
     * @param e Перехваченное исключение.
     * @return Структурированный JSON-ответ с ошибкой 409.
     */
    @ExceptionHandler(OwnerMismatchException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 403
    public ErrorResponse handleOwnerMismatchException(final OwnerMismatchException e) {
        log.error(e.getMessage(), e);
        return createErrorResponse("Нарушение доступа", e);
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
        log.error(e.getMessage(), e);
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
        log.error(e.getMessage(), e);
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
        log.error(e.getMessage(), e);
        return createErrorResponse("Непредвиденная ошибка сервера", e);
    }
}