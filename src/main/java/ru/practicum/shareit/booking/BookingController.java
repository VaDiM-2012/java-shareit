package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import java.util.List;

/**
 * Контроллер для управления бронированиями.
 */
@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                     @Valid @RequestBody BookingCreateDto dto) {
        log.info("POST /bookings (Booker: {}): Создание бронирования", bookerId);
        return bookingService.create(bookerId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveOrReject(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                              @PathVariable Long bookingId,
                                              @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{} (Owner: {}): Установка статуса approved={}", bookingId, ownerId, approved);
        return bookingService.approveOrReject(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @PathVariable Long bookingId) {
        log.info("GET /bookings/{} (User: {}): Получение бронирования", bookingId, userId);
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                   @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings (Booker: {}): Получение бронирований со статусом {}", bookerId, state);
        return bookingService.getAllByBooker(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                  @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /bookings/owner (Owner: {}): Получение бронирований для его вещей со статусом {}", ownerId, state);
        return bookingService.getAllByOwner(ownerId, state, from, size);
    }
}