package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

/**
 * Маппер для преобразования между моделью Booking и DTO BookingDto.
 * Заглушка. Реализация будет добавлена в спринте add-bookings.
 */
public final class BookingMapper {

    private BookingMapper() {

    }

    /**
     * Преобразует модель Booking в DTO BookingDto.
     *
     * @param booking Модель бронирования.
     * @return DTO бронирования.
     */
    public static BookingDto toBookingDto(Booking booking) {

        return null;
    }

    /**
     * Преобразует DTO BookingDto в модель Booking.
     *
     * @param dto DTO бронирования.
     * @return Модель бронирования.
     */
    public static Booking toBooking(BookingDto dto) {

        return null;
    }
}