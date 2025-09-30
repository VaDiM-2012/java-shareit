package ru.practicum.shareit.request.dto;


import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * DTO для сущности Запрос на вещь (ItemRequest).
 * Используется record для обеспечения иммутабельности.
 */
public record ItemRequestDto(
        /** Уникальный идентификатор запроса. */
        Long id,
        /** Описание требуемой вещи. Обязательное поле. */
        @NotBlank(message = "Описание запроса не может быть пустым.")
        String description,
        /** Дата и время создания запроса. */
        LocalDateTime created
) {
}