package ru.practicum.shareit.booking.model;

/**
 * Перечисление статусов бронирования.
 */
public enum BookingStatus {
    /** Ожидает подтверждения владельцем. */
    WAITING,
    /** Бронирование подтверждено владельцем. */
    APPROVED,
    /** Бронирование отклонено владельцем. */
    REJECTED,
    /** Бронирование отменено создателем. */
    CANCELED
}