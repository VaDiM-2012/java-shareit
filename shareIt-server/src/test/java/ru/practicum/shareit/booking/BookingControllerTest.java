package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.OwnerMismatchException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для {@link BookingController}.
 * Проверяет обработку HTTP-запросов и возвращаемые ответы контроллера.
 */
@WebMvcTest(controllers = BookingController.class)
@ActiveProfiles("test")
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Тестирует создание бронирования (POST /bookings) с корректными данными.
     * Ожидается статус 201 и корректный JSON-ответ.
     */
    @Test
    void createBooking_ValidData_ReturnsCreated() throws Exception {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto bookingDto = new BookingCreateDto(1L, start, end);
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, start, end, BookingStatus.WAITING,
                new UserDto(1L, "User", "user@example.com"),
                new ItemDto(1L, "Drill", "Electric drill", true, null)
        );
        when(bookingService.create(eq(1L), any(BookingCreateDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is(1)))
                .andExpect(jsonPath("$.item.id", is(1)));

        verify(bookingService, times(1)).create(eq(1L), any(BookingCreateDto.class));
    }

    /**
     * Тестирует создание бронирования с некорректными датами (POST /bookings).
     * Ожидается статус 400 и сообщение об ошибке.
     */
    @Test
    void createBooking_InvalidDates_ReturnsBadRequest() throws Exception {
        // Arrange
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookingCreateDto bookingDto = new BookingCreateDto(1L, start, end);
        when(bookingService.create(eq(1L), any(BookingCreateDto.class)))
                .thenThrow(new ItemNotAvailableException("Дата начала должна быть раньше окончания."));

        // Act & Assert
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Вещь недоступна")))
                .andExpect(jsonPath("$.description", is("Дата начала должна быть раньше окончания.")));

        verify(bookingService, times(1)).create(eq(1L), any(BookingCreateDto.class));
    }

    /**
     * Тестирует подтверждение/отклонение бронирования (PATCH /bookings/{bookingId}).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void approveOrRejectBooking_ValidData_ReturnsOk() throws Exception {
        // Arrange
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED, new UserDto(1L, "User", "user@example.com"),
                new ItemDto(1L, "Drill", "Electric drill", true, null)
        );
        when(bookingService.approveOrReject(1L, 1L, true)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(bookingService, times(1)).approveOrReject(1L, 1L, true);
    }

    /**
     * Тестирует подтверждение/отклонение бронирования с неверным владельцем (PATCH /bookings/{bookingId}).
     * Ожидается статус 403 и сообщение об ошибке.
     */
    @Test
    void approveOrRejectBooking_WrongOwner_ReturnsForbidden() throws Exception {
        // Arrange
        when(bookingService.approveOrReject(1L, 1L, true))
                .thenThrow(new OwnerMismatchException("Пользователь не является владельцем вещи."));

        // Act & Assert
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Нарушение доступа")))
                .andExpect(jsonPath("$.description", is("Пользователь не является владельцем вещи.")));

        verify(bookingService, times(1)).approveOrReject(1L, 1L, true);
    }

    /**
     * Тестирует получение бронирования по ID (GET /bookings/{bookingId}).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getBookingById_ValidId_ReturnsOk() throws Exception {
        // Arrange
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING, new UserDto(1L, "User", "user@example.com"),
                new ItemDto(1L, "Drill", "Electric drill", true, null)
        );
        when(bookingService.getById(1L, 1L)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")));

        verify(bookingService, times(1)).getById(1L, 1L);
    }

    /**
     * Тестирует получение бронирований арендатора (GET /bookings).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getAllBookingsByBooker_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        List<BookingResponseDto> bookings = List.of(
                new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                        BookingStatus.WAITING, new UserDto(1L, "User", "user@example.com"),
                        new ItemDto(1L, "Drill", "Electric drill", true, null))
        );
        when(bookingService.getAllByBooker(1L, "ALL", 0, 10)).thenReturn(bookings);

        // Act & Assert
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")));

        verify(bookingService, times(1)).getAllByBooker(1L, "ALL", 0, 10);
    }

    /**
     * Тестирует получение бронирований арендатора с некорректным state (GET /bookings).
     * Ожидается статус 409 и сообщение об ошибке.
     */
    @Test
    void getAllBookingsByBooker_InvalidState_ReturnsConflict() throws Exception {
        // Arrange
        when(bookingService.getAllByBooker(1L, "INVALID", 0, 10))
                .thenThrow(new ValidationException("Unknown state: INVALID"));

        // Act & Assert
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "INVALID")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Нарушение бизнес-правил")))
                .andExpect(jsonPath("$.description", is("Unknown state: INVALID")));

        verify(bookingService, times(1)).getAllByBooker(1L, "INVALID", 0, 10);
    }

    /**
     * Тестирует получение бронирований владельца (GET /bookings/owner).
     * Ожидается статус 200 и корректный JSON-ответ.
     */
    @Test
    void getAllBookingsByOwner_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        List<BookingResponseDto> bookings = List.of(
                new BookingResponseDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                        BookingStatus.WAITING, new UserDto(1L, "User", "user@example.com"),
                        new ItemDto(1L, "Drill", "Electric drill", true, null))
        );
        when(bookingService.getAllByOwner(1L, "ALL", 0, 10)).thenReturn(bookings);

        // Act & Assert
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].status", is("WAITING")));

        verify(bookingService, times(1)).getAllByOwner(1L, "ALL", 0, 10);
    }
}