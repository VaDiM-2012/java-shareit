package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link ItemController}.
 * Проверяет обработку HTTP-запросов и возвращаемые ответы контроллера.
 */
@WebMvcTest(controllers = ItemController.class)
@ActiveProfiles("test")
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Тестирует создание вещи (POST /items) с корректными данными.
     * Ожидается статус 201 и корректный JSON-ответ.
     */
    @Test
    void createItem_ValidData_ReturnsCreated() throws Exception {
        // Arrange
        ItemDto itemDto = new ItemDto(null, "Drill", "Electric drill", true, null);
        ItemDto createdItem = new ItemDto(1L, "Drill", "Electric drill", true, null);
        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(createdItem);

        // Act & Assert
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Electric drill")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService, times(1)).create(eq(1L), any(ItemDto.class));
    }

    /**
     * Тестирует создание вещи с некорректными данными (POST /items).
     * Ожидается статус 400 и сообщение об ошибке валидации.
     */
    @Test
    void createItem_InvalidData_ReturnsBadRequest() throws Exception {
        // Arrange
        ItemDto itemDto = new ItemDto(null, "", "", null, null);

        // Act & Assert
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка валидации DTO")));

        verify(itemService, never()).create(anyLong(), any(ItemDto.class));
    }

    /**
     * Тестирует обновление вещи (PATCH /items/{itemId}) с корректными данными.
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void updateItem_ValidData_ReturnsOk() throws Exception {
        // Arrange
        ItemDto itemDto = new ItemDto(null, "Updated Drill", "Updated description", true, null);
        ItemDto updatedItem = new ItemDto(1L, "Updated Drill", "Updated description", true, null);
        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(updatedItem);

        // Act & Assert
        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Drill")))
                .andExpect(jsonPath("$.description", is("Updated description")));

        verify(itemService, times(1)).update(eq(1L), eq(1L), any(ItemDto.class));
    }

    /**
     * Тестирует обновление вещи с несуществующим ID (PATCH /items/{itemId}).
     * Ожидается статус 404 и сообщение об ошибке.
     */
    @Test
    void updateItem_NotFound_ReturnsNotFound() throws Exception {
        // Arrange
        ItemDto itemDto = new ItemDto(null, "Updated Drill", "Updated description", true, null);
        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class)))
                .thenThrow(new NotFoundException("Вещь ID 1 не найдена."));

        // Act & Assert
        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Объект не найден")))
                .andExpect(jsonPath("$.description", is("Вещь ID 1 не найдена.")));

        verify(itemService, times(1)).update(eq(1L), eq(1L), any(ItemDto.class));
    }

    /**
     * Тестирует получение вещи по ID (GET /items/{itemId}).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getItemById_ValidId_ReturnsOk() throws Exception {
        // Arrange
        ItemResponseDto itemResponseDto = new ItemResponseDto(1L, "Drill", "Electric drill", true, null, null, null, List.of());
        when(itemService.getById(1L, 1L)).thenReturn(itemResponseDto);

        // Act & Assert
        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Electric drill")));

        verify(itemService, times(1)).getById(1L, 1L);
    }

    /**
     * Тестирует получение всех вещей владельца (GET /items).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getAllItemsByOwner_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        List<ItemResponseDto> items = List.of(
                new ItemResponseDto(1L, "Drill", "Electric drill", true, null, null, null, List.of()),
                new ItemResponseDto(2L, "Hammer", "Heavy hammer", true, null, null, null, List.of())
        );
        when(itemService.getAllByOwner(1L, 0, 10)).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Hammer")));

        verify(itemService, times(1)).getAllByOwner(1L, 0, 10);
    }

    /**
     * Тестирует поиск вещей (GET /items/search).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void searchItems_ValidText_ReturnsOk() throws Exception {
        // Arrange
        List<ItemDto> items = List.of(
                new ItemDto(1L, "Drill", "Electric drill", true, null),
                new ItemDto(2L, "Driller", "Another drill", true, null)
        );
        when(itemService.search("drill", 0, 10)).thenReturn(items);

        // Act & Assert
        mockMvc.perform(get("/items/search")
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Driller")));

        verify(itemService, times(1)).search("drill", 0, 10);
    }

    /**
     * Тестирует добавление комментария (POST /items/{itemId}/comment).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void addComment_ValidData_ReturnsOk() throws Exception {
        // Arrange
        CommentCreateDto commentDto = new CommentCreateDto("Great item!");
        CommentDto createdComment = new CommentDto(1L, "Great item!", "Test User", LocalDateTime.now());
        when(itemService.addComment(eq(1L), eq(1L), any(CommentCreateDto.class))).thenReturn(createdComment);

        // Act & Assert
        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great item!")))
                .andExpect(jsonPath("$.authorName", is("Test User")));

        verify(itemService, times(1)).addComment(eq(1L), eq(1L), any(CommentCreateDto.class));
    }

    /**
     * Тестирует добавление комментария без завершенного бронирования (POST /items/{itemId}/comment).
     * Ожидается статус 400 и сообщение об ошибке.
     */
    @Test
    void addComment_NoBooking_ReturnsBadRequest() throws Exception {
        // Arrange
        CommentCreateDto commentDto = new CommentCreateDto("Great item!");
        when(itemService.addComment(eq(1L), eq(1L), any(CommentCreateDto.class)))
                .thenThrow(new BookingNotFoundException("Пользователь не бронировал вещь."));

        // Act & Assert
        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Ошибка комментирования")))
                .andExpect(jsonPath("$.description", is("Пользователь не бронировал вещь.")));

        verify(itemService, times(1)).addComment(eq(1L), eq(1L), any(CommentCreateDto.class));
    }
}