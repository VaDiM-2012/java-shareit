package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collections;
import java.util.List;

/**
 * Сервис для управления сущностью Booking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    /**
     * Создает новое бронирование.
     *
     * @param userId ID пользователя, создающего бронирование (booker).
     * @param dto DTO с данными бронирования.
     * @return DTO созданного бронирования.
     * @throws UnsupportedOperationException .
     */
    public BookingDto createBooking(Long userId, BookingDto dto) {
        log.info("Получен запрос на создание бронирования от пользователя {} с данными: {}", userId, dto);
        throw new UnsupportedOperationException("");
    }

    /**
     * Обновляет статус бронирования (подтверждение/отклонение).
     *
     * @param bookingId ID бронирования.
     * @param userId ID пользователя-владельца вещи.
     * @param approved Статус: true - подтвердить, false - отклонить.
     * @return DTO обновленного бронирования.
     * @throws UnsupportedOperationException .
     */
    public BookingDto updateBookingStatus(Long bookingId, Long userId, Boolean approved) {
        log.info("Получен запрос на обновление статуса бронирования {} пользователем {} со статусом approved={}", bookingId, userId, approved);
        throw new UnsupportedOperationException("");
    }

    /**
     * Возвращает бронирование по ID.
     *
     * @param bookingId ID бронирования.
     * @param userId ID пользователя, запрашивающего информацию (должен быть booker или owner).
     * @return DTO найденного бронирования.
     * @throws UnsupportedOperationException .
     */
    public BookingDto getBookingById(Long bookingId, Long userId) {
        log.info("Получен запрос на получение бронирования {} пользователем {}", bookingId, userId);
        throw new UnsupportedOperationException("");
    }

    /**
     * Возвращает список всех бронирований для конкретного арендатора.
     *
     * @param userId ID арендатора (booker).
     * @param state Строка состояния (e.g., ALL, CURRENT, PAST).
     * @return Список DTO бронирований.
     * @throws UnsupportedOperationException .
     */
    public List<BookingDto> getAllBookingsByUser(Long userId, String state) {
        log.info("Получен запрос на получение списка бронирований для арендатора {} со статусом {}", userId, state);
        // Возвращаем пустой список вместо исключения, так как это GET-запрос списка
        return Collections.emptyList();

    }

    /**
     * Возвращает список бронирований для вещей конкретного владельца.
     *
     * @param userId ID владельца (owner).
     * @param state Строка состояния (e.g., ALL, CURRENT, PAST).
     * @return Список DTO бронирований.
     * @throws UnsupportedOperationException .
     */
    public List<BookingDto> getAllBookingsByOwner(Long userId, String state) {
        log.info("Получен запрос на получение списка бронирований для владельца {} со статусом {}", userId, state);
        // Возвращаем пустой список вместо исключения, так как это GET-запрос списка
        return Collections.emptyList();
    }
}