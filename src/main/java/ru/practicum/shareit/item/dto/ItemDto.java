package ru.practicum.shareit.item.dto;

/**
 * Базовый DTO для Вещи (используется как вложенный DTO в BookingResponseDto).
 */
public record ItemDto(
        Long id,
        String name,
        String description,
        Boolean available,
        Long requestId
) {}