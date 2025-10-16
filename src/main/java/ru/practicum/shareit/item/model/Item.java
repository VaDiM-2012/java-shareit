package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

/**
 * Основная модель сущности Вещь (Item).
 * Представляет предмет, который может быть арендован.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    /** Уникальный идентификатор вещи. */
    private Long id;

    /** Краткое название. Не может быть пустым. */
    private String name;

    /** Развернутое описание. Не может быть пустым. */
    private String description;

    /** Статус доступности для аренды. Обязательное поле. */
    private Boolean available;

    /** Владелец вещи. Обязательное поле. */
    private User owner;

    /** Ссылка на запрос, если вещь создана в ответ на запрос. Опционально. */
    private ItemRequest request;
}