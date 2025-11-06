package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link ItemRequestController}.
 * Проверяет обработку HTTP-запросов и возвращаемые ответы контроллера.
 */
@WebMvcTest(controllers = ItemRequestController.class)
@ActiveProfiles("test")
public class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Тестирует создание запроса на вещь (POST /requests) с корректными данными.
     * Ожидается статус 201 и корректный JSON-ответ.
     */
    @Test
    void createRequest_ValidData_ReturnsCreated() throws Exception {
        // Arrange
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto("Need a drill");
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(1L, "Need a drill", LocalDateTime.now(), List.of());
        when(requestService.create(eq(1L), any(ItemRequestCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a drill")));

        verify(requestService, times(1)).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    /**
     * Тестирует создание запроса с некорректным описанием (POST /requests).
     * Ожидается статус 400 и сообщение об ошибке валидации.
     */
    @Test
    void createRequest_InvalidData_ReturnsBadRequest() throws Exception {
        // Arrange
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto("");

        // Act & Assert
        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации DTO")))
                .andExpect(jsonPath("$.description", is("Описание запроса не может быть пустым.")));

        verify(requestService, never()).create(anyLong(), any(ItemRequestCreateDto.class));
    }

    /**
     * Тестирует получение всех запросов пользователя (GET /requests).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getAllRequestsByRequestor_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        List<ItemRequestResponseDto> requests = List.of(
                new ItemRequestResponseDto(1L, "Need a drill", LocalDateTime.now(), List.of())
        );
        when(requestService.getAllByRequestor(1L)).thenReturn(requests);

        // Act & Assert
        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Need a drill")));

        verify(requestService, times(1)).getAllByRequestor(1L);
    }

    /**
     * Тестирует получение всех чужих запросов (GET /requests/all).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getAllRequests_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        List<ItemRequestResponseDto> requests = List.of(
                new ItemRequestResponseDto(1L, "Need a drill", LocalDateTime.now(), List.of())
        );
        when(requestService.getAll(1L, 0, 10)).thenReturn(requests);

        // Act & Assert
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Need a drill")));

        verify(requestService, times(1)).getAll(1L, 0, 10);
    }

    /**
     * Тестирует получение запроса по ID (GET /requests/{requestId}).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getRequestById_ValidId_ReturnsOk() throws Exception {
        // Arrange
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto(1L, "Need a drill", LocalDateTime.now(), List.of());
        when(requestService.getById(1L, 1L)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a drill")));

        verify(requestService, times(1)).getById(1L, 1L);
    }

    /**
     * Тестирует получение запроса с несуществующим ID (GET /requests/{requestId}).
     * Ожидается статус 404 и сообщение об ошибке.
     */
    @Test
    void getRequestById_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(requestService.getById(1L, 1L))
                .thenThrow(new NotFoundException("Запрос ID 1 не найден."));

        // Act & Assert
        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Объект не найден")))
                .andExpect(jsonPath("$.description", is("Запрос ID 1 не найден.")));

        verify(requestService, times(1)).getById(1L, 1L);
    }
}