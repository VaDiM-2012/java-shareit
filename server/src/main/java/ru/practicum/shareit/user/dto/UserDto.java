package ru.practicum.shareit.user.dto;

/**
 * DTO для Пользователя.
 */
public record UserDto(
        Long id,
        String name,
        String email
) {}