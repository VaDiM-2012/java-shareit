package ru.practicum.shareit.request.dto;

/**
 * DTO для создания нового Запроса на вещь.
 */
public record ItemRequestCreateDto(
        String description
) {}