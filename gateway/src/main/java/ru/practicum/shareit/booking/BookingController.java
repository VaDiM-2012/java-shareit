package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Получает список бронирований пользователя.
     *
     * @param bookerId идентификатор пользователя.
     * @param stateParam состояние бронирований.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком бронирований.
     * @throws IllegalArgumentException если неизвестное состояние.
     */
    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                                 @RequestParam(defaultValue = "ALL") String stateParam,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                 @Positive @RequestParam(defaultValue = "10") int size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, bookerId, from, size);
        return bookingClient.getBookings(bookerId, state, from, size);
    }

    /**
     * Создает новое бронирование.
     *
     * @param bookerId идентификатор пользователя.
     * @param requestDto данные для бронирования.
     * @return ответ с созданным бронированием.
     */
    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                         @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("POST /bookings (Booker: {}): Создание бронирования", bookerId);
        return bookingClient.bookItem(bookerId, requestDto);
    }

    /**
     * Получает информацию о бронировании по идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @param bookingId идентификатор бронирования.
     * @return ответ с данными бронирования.
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long bookingId) {
        log.info("GET /bookings/{} (User: {}): Получение бронирования", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    /**
     * Одобряет или отклоняет бронирование.
     *
     * @param ownerId идентификатор владельца.
     * @param bookingId идентификатор бронирования.
     * @param approved флаг одобрения.
     * @return ответ с обновленным бронированием.
     */
    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveOrReject(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                                  @PathVariable Long bookingId,
                                                  @RequestParam Boolean approved) {
        log.info("PATCH /bookings/{} (Owner: {}): Установка статуса approved={}", bookingId, ownerId, approved);
        return bookingClient.approveOrReject(ownerId, bookingId, approved);
    }

    /**
     * Получает список бронирований владельца.
     *
     * @param ownerId идентификатор владельца.
     * @param stateParam состояние бронирований.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком бронирований.
     * @throws IllegalArgumentException если неизвестное состояние.
     */
    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                                @RequestParam(defaultValue = "ALL") String stateParam,
                                                @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                @Positive @RequestParam(defaultValue = "10") int size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get owner bookings with state {}, userId={}, from={}, size={}", stateParam, ownerId, from, size);
        return bookingClient.getAllByOwner(ownerId, state, from, size);
    }
}