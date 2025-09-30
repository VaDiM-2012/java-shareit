package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Объект передачи данных (DTO) для сущности Item.
 * Используется для обмена данными между контроллером и сервисом,
 * содержит аннотации Bean Validation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    /** Уникальный идентификатор вещи. */
    private Long id;

    /** Краткое название. */
    @NotBlank(message = "Название не может быть пустым.")
    private String name;

    /** Развернутое описание. */
    @NotBlank(message = "Описание не может быть пустым.")
    private String description;

    /** Статус доступности для аренды. */
    @NotNull(message = "Статус доступности (available) должен быть указан.")
    private Boolean available;

    /** ID связанного запроса. Может быть null. */
    private Long requestId;
}