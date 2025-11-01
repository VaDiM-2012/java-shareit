package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностью {@link User} и ее DTO.
 */
public class UserMapper {

    private UserMapper() {
        // Утилитарный класс
    }

    /**
     * Преобразует сущность {@link User} в DTO {@link UserDto}.
     *
     * @param user Сущность пользователя.
     * @return DTO пользователя.
     */
    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    /**
     * Преобразует коллекцию сущностей {@link User} в коллекцию DTO {@link UserDto}.
     *
     * @param users Список сущностей пользователей.
     * @return Список DTO пользователей.
     */
    public static List<UserDto> toDto(List<User> users) {
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует DTO {@link UserDto} в сущность {@link User}.
     *
     * @param dto DTO пользователя.
     * @return Сущность пользователя.
     */
    public static User toEntity(UserDto dto) {
        return new User(
                dto.id(),
                dto.name(),
                dto.email()
        );
    }
}