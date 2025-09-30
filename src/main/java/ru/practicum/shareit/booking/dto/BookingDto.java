package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;

/**
 * DTO для сущности Бронирование (Booking).
 * Используется record для обеспечения иммутабельности.
 */
public record BookingDto(
        // Уникальный идентификатор бронирования.
        Long id,

        // Дата и время начала аренды. Обязательное поле.
        @NotNull(message = "Дата начала бронирования не может быть null.")
        LocalDateTime start,

        // Дата и время окончания аренды. Обязательное поле.
        @NotNull(message = "Дата окончания бронирования не может быть null.")
        LocalDateTime end,

        // ID вещи, которую бронируют. Обязательное поле.
        @NotNull(message = "ID вещи не может быть null.")
        Long itemId,

        // ID пользователя-арендатора. Обязательное поле.
        @NotNull(message = "ID арендатора не может быть null.")
        Long bookerId,

        // Статус бронирования. Обязательное поле.
        @NotNull(message = "Статус бронирования не может быть null.")
        BookingStatus status
) {
}