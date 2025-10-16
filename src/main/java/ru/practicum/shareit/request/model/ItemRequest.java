package ru.practicum.shareit.request.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * Модель сущности Запрос на вещь (ItemRequest).
 * Содержит описание требуемой вещи и информацию о пользователе-запросчике.
 */
@Getter
@Setter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ItemRequest {
    /** Уникальный идентификатор запроса. */
    private Long id;

    /** Описание требуемой вещи. Неизменяемое поле. */
    private final String description;

    /** Пользователь, который создал запрос. Неизменяемое поле. */
    private final User requestor;

    /** Дата и время создания запроса. Неизменяемое поле. */
    private final LocalDateTime created;
}