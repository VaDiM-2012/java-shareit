package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

/**
 * DTO для ответа о Вещи (используется для GET /items/{id} и GET /items),
 * включает данные о бронировании и комментарии.
 */
public record ItemResponseDto(
        Long id,
        String name,
        String description,
        Boolean available,
        Long requestId,
        BookingInItemDto lastBooking,
        BookingInItemDto nextBooking,
        List<CommentDto> comments
) {}