package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

/**
 * Модель сущности Бронирование (Booking).
 * Содержит информацию о периоде аренды, вещи, арендаторе и статусе.
 */
@Getter
@Setter // Setter нужен для поля status, которое меняется
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Booking {
    /** Уникальный идентификатор бронирования. */
    private Long id;

    /** Дата и время начала аренды. Неизменяемое поле. */
    private final LocalDateTime start;

    /** Дата и время окончания аренды. Неизменяемое поле. */
    private final LocalDateTime end;

    /** Вещь, которую бронируют. Неизменяемое поле. */
    private final Item item;

    /** Пользователь, который бронирует вещь. Неизменяемое поле. */
    private final User booker;

    /** Текущий статус бронирования (может меняться). */
    private BookingStatus status;
}