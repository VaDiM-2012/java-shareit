package ru.practicum.shareit.item.dto;

import java.time.LocalDateTime;

/**
 * Упрощенный DTO бронирования для включения в DTO вещи.
 */
public record BookingInItemDto(
        Long id,
        Long bookerId,
        LocalDateTime start,
        LocalDateTime end
) {}