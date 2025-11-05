package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым.")
    @Size(max = 2000, message = "Текст комментария слишком длинный.") // Ограничение на всякий случай
    private String text;
}