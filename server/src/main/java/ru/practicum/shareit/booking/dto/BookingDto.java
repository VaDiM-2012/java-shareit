package ru.practicum.shareit.booking.dto;

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
        LocalDateTime start,
        LocalDateTime end,
        Long itemId,
        Long bookerId,
        BookingStatus status
) {
}