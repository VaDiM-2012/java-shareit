package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.validation.CreateGroup;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    /**
     * Создает нового пользователя.
     *
     * @param userDto данные пользователя.
     * @return ответ с созданным пользователем.
     */
    @PostMapping
    public ResponseEntity<Object> create(
            @Validated({Default.class, CreateGroup.class})
            @Valid @RequestBody UserRequestDto userDto) {
        log.info("POST /users: Создание пользователя с email {}", userDto.getEmail());
        return userClient.createUser(userDto);
    }

    /**
     * Обновляет пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param userDto данные для обновления.
     * @return ответ с обновленным пользователем.
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(
            @PathVariable Long userId,
            @Validated @RequestBody UserRequestDto userDto) {
        log.info("PATCH /users/{}: Обновление пользователя", userId);
        return userClient.updateUser(userId, userDto);
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @return ответ с данными пользователя.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable Long userId) {
        log.info("GET /users/{}: Получение пользователя", userId);
        return userClient.getUser(userId);
    }

    /**
     * Получает список всех пользователей.
     *
     * @return ответ с списком пользователей.
     */
    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("GET /users: Получение всех пользователей");
        return userClient.getAllUsers();
    }

    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя.
     * @return ответ об успешном удалении.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        log.info("DELETE /users/{}: Удаление пользователя", userId);
        return userClient.deleteUser(userId);
    }
}