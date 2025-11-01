package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.BookingInItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностью {@link Item} и ее DTO.
 */
public class ItemMapper {

    private ItemMapper() {
        // Утилитарный класс
    }

    /**
     * Преобразует сущность {@link Item} в базовый DTO {@link ItemDto}.
     *
     * @param item Сущность вещи.
     * @return Базовый DTO вещи.
     */
    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    /**
     * Преобразует сущность {@link Item} в расширенный DTO {@link ItemResponseDto}.
     *
     * @param item Сущность вещи.
     * @param lastBooking DTO последнего бронирования.
     * @param nextBooking DTO следующего бронирования.
     * @param comments Список DTO комментариев.
     * @return Расширенный DTO вещи.
     */
    public static ItemResponseDto toDto(
            Item item,
            BookingInItemDto lastBooking,
            BookingInItemDto nextBooking,
            List<CommentDto> comments) {
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                lastBooking,
                nextBooking,
                comments != null ? comments : Collections.emptyList()
        );
    }

    /**
     * Преобразует коллекцию сущностей {@link Item} в коллекцию базовых DTO {@link ItemDto}.
     *
     * @param items Список сущностей вещей.
     * @return Список базовых DTO вещей.
     */
    public static List<ItemDto> toDto(List<Item> items) {
        return items.stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует DTO {@link ItemDto} в сущность {@link Item}.
     *
     * @param dto DTO вещи.
     * @return Сущность вещи.
     */
    public static Item toEntity(ItemDto dto) {
        Item item = new Item();
        item.setId(dto.id());
        item.setName(dto.name());
        item.setDescription(dto.description());
        item.setAvailable(dto.available());
        return item;
    }
}