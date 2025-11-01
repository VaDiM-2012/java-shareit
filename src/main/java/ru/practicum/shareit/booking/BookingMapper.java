package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для бронирований.
 */
public class BookingMapper {
    private BookingMapper() {

    }

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

    public static List<BookingResponseDto> toDto(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }
}