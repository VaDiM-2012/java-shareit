package ru.practicum.shareit.user;

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

    @PostMapping
    public UserDto create(@Validated({Default.class, CreateGroup.class}) @RequestBody UserDto userDto) {
        log.info("POST /users: Создание пользователя");
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable Long userId, @Validated @RequestBody UserDto userDto) {
        log.info("PATCH /users/{}: Обновление пользователя", userId);
        return userService.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable Long userId) {
        log.info("GET /users/{}: Получение пользователя", userId);
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("GET /users: Получение всех пользователей");
        return userService.getAll();
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("DELETE /users/{}: Удаление пользователя", userId);
        userService.delete(userId);
    }
}