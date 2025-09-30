package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * REST-контроллер для обработки запросов, связанных с сущностью User.
 * Основной путь: /users.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * POST /users - Создание нового пользователя.
     *
     * @param userDto DTO с данными нового пользователя.
     * @return DTO созданного пользователя.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    /**
     * PATCH /users/{id} - Обновление существующего пользователя (частичное обновление).
     *
     * @param id ID пользователя для обновления.
     * @param userDto DTO с обновляемыми данными.
     * @return DTO обновленного пользователя.
     */
    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto userDto) {
        return userService.update(id, userDto);
    }

    /**
     * GET /users - Получение списка всех пользователей.
     *
     * @return List DTO всех пользователей.
     */
    @GetMapping
    public List<UserDto> findAll() {
        return userService.findAll();
    }

    /**
     * GET /users/{id} - Получение пользователя по ID.
     *
     * @param id ID пользователя.
     * @return DTO найденного пользователя.
     */
    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /**
     * DELETE /users/{id} - Удаление пользователя по ID.
     *
     * @param id ID пользователя для удаления.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        userService.deleteById(id);
    }
}