package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;


import java.util.List;

/**
 * REST-контроллер для обработки запросов, связанных с сущностью Booking.
 * Заглушка. Полная реализация будет добавлена в спринте add-bookings.
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /bookings - Создание нового бронирования.
     *
     * @param userId ID пользователя, создающего бронирование.
     * @param dto DTO с данными бронирования.
     * @return DTO созданного бронирования.
     * @throws UnsupportedOperationException Метод будет реализован в спринте add-bookings.
     */
    @PostMapping
    public BookingDto createBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody BookingDto dto) {
        log.info("Endpoint POST /bookings: Request to create booking by user {}", userId);
        return bookingService.createBooking(userId, dto);
    }

    /**
     * PATCH /bookings/{bookingId} - Подтверждение или отклонение запроса на бронирование.
     *
     * @param bookingId ID бронирования.
     * @param userId ID пользователя-владельца.
     * @param approved Статус: true - подтвердить, false - отклонить.
     * @return DTO обновленного бронирования.
     * @throws UnsupportedOperationException Метод будет реализован в спринте add-bookings.
     */
    @PatchMapping("/{bookingId}")
    public BookingDto updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam Boolean approved) {
        log.info("Endpoint PATCH /bookings/{}: Request to update status to {} by owner {}", bookingId, approved, userId);
        return bookingService.updateBookingStatus(bookingId, userId, approved);
    }

    /**
     * GET /bookings/{bookingId} - Получение данных о конкретном бронировании.
     *
     * @param bookingId ID бронирования.
     * @param userId ID пользователя, запрашивающего данные.
     * @return DTO найденного бронирования.
     * @throws UnsupportedOperationException Метод будет реализован в спринте add-bookings.
     */
    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Endpoint GET /bookings/{}: Request to get booking by user {}", bookingId, userId);
        return bookingService.getBookingById(bookingId, userId);
    }

    /**
     * GET /bookings - Получение списка бронирований текущего пользователя (арендатора).
     *
     * @param userId ID пользователя (арендатора).
     * @param state Строка состояния (ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED).
     * @return Список DTO бронирований.
     * @throws UnsupportedOperationException Метод будет реализован в спринте add-bookings.
     */
    @GetMapping
    public List<BookingDto> getAllBookingsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Endpoint GET /bookings: Request to get all bookings for booker {} with state {}", userId, state);
        return bookingService.getAllBookingsByUser(userId, state);
    }

    /**
     * GET /bookings/owner - Получение списка бронирований для вещей текущего пользователя (владельца).
     *
     * @param userId ID пользователя (владельца).
     * @param state Строка состояния.
     * @return Список DTO бронирований.
     * @throws UnsupportedOperationException Метод будет реализован в спринте add-bookings.
     */
    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Endpoint GET /bookings/owner: Request to get all bookings for owner {} with state {}", userId, state);
        return bookingService.getAllBookingsByOwner(userId, state);
    }
}