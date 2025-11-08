package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {
    private BookingMapper() {

    }

    /**
     * Преобразует модель бронирования в DTO.
     *
     * @param booking модель бронирования.
     * @return DTO бронирования.
     */
    public static BookingResponseDto toDto(Booking booking) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                UserMapper.toDto(booking.getBooker()),
                ItemMapper.toDto(booking.getItem())
        );
    }

    /**
     * Преобразует список моделей бронирований в список DTO.
     *
     * @param bookings список моделей.
     * @return список DTO.
     */
    public static List<BookingResponseDto> toDto(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }
}