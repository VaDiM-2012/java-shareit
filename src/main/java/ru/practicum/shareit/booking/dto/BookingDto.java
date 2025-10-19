package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

/**
 * DTO для бронирования.
 *
 * @param id       ID бронирования.
 * @param start    Дата начала.
 * @param end      Дата окончания.
 * @param itemId   ID вещи.
 * @param bookerId ID арендатора.
 * @param status   Статус.
 */
public record BookingDto(
        Long id,
        @NotNull(message = "Дата начала не может быть null.") LocalDateTime start,
        @NotNull(message = "Дата окончания не может быть null.") LocalDateTime end,
        @NotNull(message = "ID вещи не может быть null.") Long itemId,
        @NotNull(message = "ID арендатора не может быть null.") Long bookerId,
        @NotNull(message = "Статус не может быть null.") BookingStatus status
) {
}