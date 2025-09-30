package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

/**
 * Маппер для преобразования между моделью ItemRequest и DTO ItemRequestDto.
 * Заглушка. Реализация будет добавлена в спринте add-item-requests.
 */
public final class ItemRequestMapper {

    private ItemRequestMapper() {}

    /**
     * Преобразует модель ItemRequest в DTO ItemRequestDto.
     * @param itemRequest Модель запроса.
     * @return DTO запроса (заглушка).
     */
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        // Заглушка. Реализация будет добавлена в спринте add-item-requests.
        return null;
    }

    /**
     * Преобразует DTO ItemRequestDto в модель ItemRequest.
     * @param dto DTO запроса.
     * @return Модель запроса (заглушка).
     */
    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        // Заглушка. Реализация будет добавлена в спринте add-item-requests.
        return null;
    }
}