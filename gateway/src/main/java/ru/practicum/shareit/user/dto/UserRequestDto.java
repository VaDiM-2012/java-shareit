package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.CreateGroup;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    Long id;

    @NotNull(groups = CreateGroup.class, message = "Имя пользователя не может быть пустым")
    @NotBlank(groups = CreateGroup.class, message = "Имя пользователя не может быть пустым")
    @Size(max = 255, message = "Имя пользователя не может быть длиннее 255 символов")
    String name;

    @NotNull(groups = CreateGroup.class, message = "Email не может быть пустым")
    @NotBlank(groups = CreateGroup.class, message = "Email не может быть пустым")
    @Size(max = 512, message = "Email не может быть длиннее 512 символов")
    @Email(message = "Некорректный email")
    String email;
}