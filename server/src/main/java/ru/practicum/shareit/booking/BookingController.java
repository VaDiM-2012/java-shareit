package ru.practicum.shareit.booking;

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

    /**
     * Создаёт новое бронирование.
     *
     * @param bookerId ID пользователя, который создаёт бронирование.
     * @param dto      DTO с данными для создания бронирования.
     * @return DTO созданного бронирования.
     */
    @PostMapping
    public BookingResponseDto create(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                     @RequestBody BookingCreateDto dto) {
        log.info("Вызван метод создания бронирования: " +
                        "арендатор (ID) = {}, " +
                        "ID вещи = {}, " +
                        "дата начала = '{}', " +
                        "дата окончания = '{}'",
                bookerId, dto.itemId(), dto.start(), dto.end());
        return bookingService.create(bookerId, dto);
    }

    /**
     * Подтверждает или отклоняет бронирование.
     *
     * @param ownerId    ID владельца вещи.
     * @param bookingId  ID бронирования.
     * @param approved   true — подтвердить, false — отклонить.
     * @return Обновлённое бронирование.
     */
    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveOrReject(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                              @PathVariable Long bookingId,
                                              @RequestParam Boolean approved) {
        log.info("Вызван метод подтверждения/отклонения бронирования: " +
                        "владелец (ID) = {}, " +
                        "ID бронирования = {}, " +
                        "статус подтверждения = {}",
                ownerId, bookingId, approved);
        return bookingService.approveOrReject(ownerId, bookingId, approved);
    }

    /**
     * Получает бронирование по его ID.
     *
     * @param userId    ID пользователя (владельца или арендатора).
     * @param bookingId ID бронирования.
     * @return DTO бронирования.
     */
    @GetMapping("/{bookingId}")
    public BookingResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @PathVariable Long bookingId) {
        log.info("Вызван метод получения бронирования по ID: " +
                        "пользователь (ID) = {}, " +
                        "ID бронирования = {}",
                userId, bookingId);
        return bookingService.getById(userId, bookingId);
    }

    /**
     * Получает все бронирования пользователя-арендатора с пагинацией и фильтрацией по состоянию.
     *
     * @param bookerId ID арендатора.
     * @param state    Состояние бронирований (например, "ALL", "CURRENT", "PAST").
     * @param from     Начальная позиция для пагинации.
     * @param size     Количество элементов на странице.
     * @return Список DTO бронирований.
     */
    @GetMapping
    public List<BookingResponseDto> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                                   @RequestParam(defaultValue = "ALL") String state,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {
        log.info("Вызван метод получения всех бронирований арендатора: " +
                        "арендатор (ID) = {}, " +
                        "состояние фильтра = '{}', " +
                        "пагинация: смещение = {}, размер страницы = {}",
                bookerId, state, from, size);
        return bookingService.getAllByBooker(bookerId, state, from, size);
    }

    /**
     * Получает все бронирования вещей, принадлежащих пользователю, с пагинацией и фильтрацией по состоянию.
     *
     * @param ownerId ID владельца вещей.
     * @param state   Состояние бронирований.
     * @param from    Начальная позиция для пагинации.
     * @param size    Количество элементов на странице.
     * @return Список DTO бронирований.
     */
    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                                  @RequestParam(defaultValue = "ALL") String state,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        log.info("Вызван метод получения всех бронирований для вещей владельца: " +
                        "владелец (ID) = {}, " +
                        "состояние фильтра = '{}', " +
                        "пагинация: смещение = {}, размер страницы = {}",
                ownerId, state, from, size);
        return bookingService.getAllByOwner(ownerId, state, from, size);
    }
}