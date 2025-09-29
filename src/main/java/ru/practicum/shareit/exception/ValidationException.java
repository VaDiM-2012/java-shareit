package ru.practicum.shareit.exception;

/**
 * Исключение, выбрасываемое при нарушении бизнес-правил валидации.
 * Используется, например, для обработки конфликта уникальности email.
 * Соответствует HTTP-статусу 409 Conflict.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}