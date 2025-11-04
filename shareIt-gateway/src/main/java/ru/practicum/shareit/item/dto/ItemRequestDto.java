// gateway/src/main/java/ru/practicum/shareit/item/dto/ItemRequestDto.java
package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {

    @NotBlank(message = "Название вещи не может быть пустым.")
    @Size(max = 255, message = "Название вещи не может быть длиннее 255 символов.")
    private String name;

    @NotBlank(message = "Описание вещи не может быть пустым.")
    @Size(max = 1000, message = "Описание вещи не может быть длиннее 1000 символов.")
    private String description;

    @NotNull(message = "Доступность вещи должна быть указана.")
    private Boolean available;

    private Long requestId; // Опционально — для ответа на запрос
}