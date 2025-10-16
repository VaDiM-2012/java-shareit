package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * Утилитарный класс для преобразования между моделью Item и DTO ItemDto.
 */
public class ItemMapper {

    private ItemMapper() {
        // Утилитный класс не должен быть инстанцирован
    }

    /**
     * Преобразует модель Item в DTO ItemDto.
     * @param item Модель вещи.
     * @return DTO вещи.
     */
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    /**
     * Преобразует DTO ItemDto в модель Item.
     * @param itemDto DTO вещи.
     * @param owner Владелец вещи.
     * @param request Запрос, если есть (может быть null).
     * @return Модель вещи.
     */
    public static Item toItem(ItemDto itemDto, User owner, ItemRequest request) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request
        );
    }
}