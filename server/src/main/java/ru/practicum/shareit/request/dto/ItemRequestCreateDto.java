package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для создания нового Запроса на вещь.
 */
public record ItemRequestCreateDto(
        @NotBlank(message = "Описание запроса не может быть пустым.")
        @Size(max = 512, message = "Описание запроса не может превышать 512 символов.")
        String description
) {}