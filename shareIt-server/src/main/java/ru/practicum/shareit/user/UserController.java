package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.CreateGroup;

import java.util.List;

/**
 * Контроллер для управления пользователями.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Создаёт нового пользователя.
     */
    @PostMapping
    public UserDto create(
            @Validated({Default.class, CreateGroup.class})
            @Valid @RequestBody UserDto userDto) {
        log.info("Вызван метод создания пользователя: " +
                        "имя = '{}', " +
                        "email = '{}'",
                userDto.name(), userDto.email());
        return userService.create(userDto);
    }

    /**
     * Обновляет данные существующего пользователя.
     */
    @PatchMapping("/{userId}")
    public UserDto update(
            @PathVariable Long userId,
            @Validated @RequestBody UserDto userDto) {
        log.info("Вызван метод обновления пользователя: " +
                        "ID пользователя = {}, " +
                        "новые данные — имя = '{}', " +
                        "email = '{}'",
                userId, userDto.name(), userDto.email());
        return userService.update(userId, userDto);
    }

    /**
     * Получает пользователя по ID.
     */
    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        log.info("Вызван метод получения пользователя по ID: ID = {}", userId);
        return userService.getById(userId);
    }

    /**
     * Получает список всех пользователей.
     */
    @GetMapping
    public List<UserDto> getAll() {
        log.info("Вызван метод получения всех пользователей");
        return userService.getAll();
    }

    /**
     * Удаляет пользователя по ID.
     */
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("Вызван метод удаления пользователя: ID = {}", userId);
        userService.delete(userId);
    }
}