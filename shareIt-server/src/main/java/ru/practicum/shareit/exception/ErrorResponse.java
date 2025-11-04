package ru.practicum.shareit.exception;

/**
 * DTO, представляющий структурированный ответ об ошибке в формате JSON.
 * Используется record для обеспечения иммутабельности.
 */
public record ErrorResponse(
        //Сообщение об ошибке.
        String error,
        //Более подробное описание или причина (опционально).
        String description
) {
}