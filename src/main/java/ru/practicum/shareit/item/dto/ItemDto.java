package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.practicum.shareit.validation.CreateGroup;

/**
 * Базовый DTO для Вещи (используется как вложенный DTO в BookingResponseDto).
 */
public record ItemDto(
        Long id,

        @NotNull(message = "Название не может быть пустым", groups = CreateGroup.class)
        @NotBlank(message = "Название не может быть пустым", groups = CreateGroup.class)
        @Size(max = 255, message = "Название не может быть длиннее 255 символов")
        String name,

        @NotNull(message = "Описание не может быть пустым", groups = CreateGroup.class)
        @NotBlank(message = "Описание не может быть пустым", groups = CreateGroup.class)
        @Size(max = 512, message = "Описание не может быть длиннее 512 символов")
        String description,

        @NotNull(message = "Доступность не может быть пустым", groups = CreateGroup.class)
        Boolean available,
        Long requestId
) {}