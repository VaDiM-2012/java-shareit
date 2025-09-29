package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

/**
 * Утилитарный класс для преобразования между моделью User и DTO UserDto.
 */
public class UserMapper {

    private UserMapper() {
        // Утилитарный класс не должен быть инстанцирован
    }

    /**
     * Преобразует модель User в DTO UserDto.
     * @param user Модель пользователя.
     * @return DTO пользователя.
     */
    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    /**
     * Преобразует DTO UserDto в модель User.
     * @param userDto DTO пользователя.
     * @return Модель пользователя.
     */
    public static User toUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
}