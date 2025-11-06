package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.InvalidUserEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link UserController}.
 * Проверяет обработку HTTP-запросов и возвращаемые ответы контроллера.
 */
@WebMvcTest(controllers = UserController.class)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    /**
     * Тестирует создание пользователя (POST /users) с корректными данными.
     * Ожидается статус 201 и корректный JSON-ответ.
     */
    @Test
    void createUser_ValidData_ReturnsCreated() throws Exception {
        // Arrange
        UserDto userDto = new UserDto(null, "Test User", "test@example.com");
        UserDto createdUser = new UserDto(1L, "Test User", "test@example.com");
        when(userService.create(any(UserDto.class))).thenReturn(createdUser);

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).create(any(UserDto.class));
    }

    /**
     * Тестирует создание пользователя с некорректным email (POST /users).
     * Ожидается статус 400 и сообщение об ошибке валидации.
     */
    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Arrange
        UserDto userDto = new UserDto(null, "Test User", "invalid-email");

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации DTO")))
                .andExpect(jsonPath("$.description", is("Некорректный email")));

        verify(userService, never()).create(any(UserDto.class));
    }

    /**
     * Тестирует создание пользователя с существующим email (POST /users).
     * Ожидается статус 409 и сообщение о конфликте.
     */
    @Test
    void createUser_EmailConflict_ReturnsConflict() throws Exception {
        // Arrange
        UserDto userDto = new UserDto(null, "Test User", "test@example.com");
        when(userService.create(any(UserDto.class)))
                .thenThrow(new InvalidUserEmailException("Email уже существует"));

        // Act & Assert
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Конфликт уникальности")))
                .andExpect(jsonPath("$.description", is("Email уже существует")));

        verify(userService, times(1)).create(any(UserDto.class));
    }

    /**
     * Тестирует обновление пользователя (PATCH /users/{userId}) с корректными данными.
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void updateUser_ValidData_ReturnsOk() throws Exception {
        // Arrange
        UserDto userDto = new UserDto(null, "Updated User", "updated@example.com");
        UserDto updatedUser = new UserDto(1L, "Updated User", "updated@example.com");
        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated User")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        verify(userService, times(1)).update(eq(1L), any(UserDto.class));
    }

    /**
     * Тестирует обновление пользователя с несуществующим ID (PATCH /users/{userId}).
     * Ожидается статус 404 и сообщение об ошибке.
     */
    @Test
    void updateUser_UserNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        UserDto userDto = new UserDto(null, "Updated User", "updated@example.com");
        when(userService.update(eq(1L), any(UserDto.class)))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не найден."));

        // Act & Assert
        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Объект не найден")))
                .andExpect(jsonPath("$.description", is("Пользователь с ID 1 не найден.")));

        verify(userService, times(1)).update(eq(1L), any(UserDto.class));
    }

    /**
     * Тестирует получение пользователя по ID (GET /users/{userId}).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getUserById_ValidId_ReturnsOk() throws Exception {
        // Arrange
        UserDto userDto = new UserDto(1L, "Test User", "test@example.com");
        when(userService.getById(1L)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).getById(1L);
    }

    /**
     * Тестирует получение пользователя с несуществующим ID (GET /users/{userId}).
     * Ожидается статус 404 и сообщение об ошибке.
     */
    @Test
    void getUserById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(userService.getById(1L))
                .thenThrow(new NotFoundException("Пользователь с ID 1 не найден."));

        // Act & Assert
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Объект не найден")))
                .andExpect(jsonPath("$.description", is("Пользователь с ID 1 не найден.")));

        verify(userService, times(1)).getById(1L);
    }

    /**
     * Тестирует получение всех пользователей (GET /users).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getAllUsers_ReturnsOk() throws Exception {
        // Arrange
        List<UserDto> users = List.of(
                new UserDto(1L, "User1", "user1@example.com"),
                new UserDto(2L, "User2", "user2@example.com")
        );
        when(userService.getAll()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("User1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("User2")));

        verify(userService, times(1)).getAll();
    }

    /**
     * Тестирует удаление пользователя (DELETE /users/{userId}).
     * Ожидается статус 200.
     */
    @Test
    void deleteUser_ValidId_ReturnsOk() throws Exception {
        // Arrange
        doNothing().when(userService).delete(1L);

        // Act & Assert
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).delete(1L);
    }
}