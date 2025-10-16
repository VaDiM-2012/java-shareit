package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностью {@link Booking} и ее DTO.
 */
public class BookingMapper {

    private BookingMapper() {
        // Утилитарный класс
    }

    /**
     * Преобразует сущность {@link Booking} в DTO для ответа {@link BookingResponseDto}.
     *
     * @param booking Сущность бронирования.
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
     * Преобразует коллекцию сущностей {@link Booking} в коллекцию DTO {@link BookingResponseDto}.
     *
     * @param bookings Список сущностей бронирования.
     * @return Список DTO бронирований.
     */
    public static List<BookingResponseDto> toDto(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Метод не используется, но требуется для единообразия, бронирование создается через Service.
     * @throws UnsupportedOperationException Всегда.
     */
    public static Booking toEntity(BookingResponseDto dto) {
        throw new UnsupportedOperationException("Метод будет реализован в спринте add-bookings");
    }
}