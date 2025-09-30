package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object для сущности User.
 * Используется для передачи данных между контроллером и сервисом,
 * а также для валидации входящих данных в контроллере.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @Size(max = 255, message = "Имя не может быть длиннее 255 символов")
    private String name;

    @Email(message = "Email должен быть валидным")
    @Size(max = 255, message = "Email не может быть длиннее 255 символов")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
}