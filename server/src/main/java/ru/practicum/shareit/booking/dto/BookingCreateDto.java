package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

/**
 * DTO для создания нового бронирования.
 * @param itemId ID вещи.
 * @param start Дата начала.
 * @param end Дата окончания.
 */
public record BookingCreateDto(
        Long itemId,
        LocalDateTime start,
        LocalDateTime end
) {}