package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

/**
 * Маппер для преобразования между моделью ItemRequest и DTO ItemRequestDto.
 */
public final class ItemRequestMapper {

    private ItemRequestMapper() {

    }

    /**
     * Преобразует модель ItemRequest в DTO ItemRequestDto.
     * @param itemRequest Модель запроса.
     * @return DTO запроса.
     */
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return null;
    }

    /**
     * Преобразует DTO ItemRequestDto в модель ItemRequest.
     * @param dto DTO запроса.
     * @return Модель запроса.
     */
    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return null;
    }
}