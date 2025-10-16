package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

/**
 * DTO для создания нового бронирования.
 */
public record BookingCreateDto(
        Long itemId,
        LocalDateTime start,
        LocalDateTime end
) {}