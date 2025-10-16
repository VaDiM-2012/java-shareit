package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

/**
 * DTO для ответа о бронировании (содержит данные о вещи и арендаторе).
 */
public record BookingResponseDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        BookingStatus status,
        UserDto booker,
        ItemDto item
) {}