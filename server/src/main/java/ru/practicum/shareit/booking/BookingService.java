package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

/**
 * Сервис для бронирований.
 */
public interface BookingService {
    enum BookingState { ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED }

    BookingResponseDto create(Long bookerId, BookingCreateDto dto);

    BookingResponseDto approveOrReject(Long ownerId, Long bookingId, Boolean approved);

    BookingResponseDto getById(Long userId, Long bookingId);

    List<BookingResponseDto> getAllByBooker(Long bookerId, String state, int from, int size);

    List<BookingResponseDto> getAllByOwner(Long ownerId, String state, int from, int size);
}