package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;

/**
 * Модель данных Бронирование (Booking).
 */
@Getter
@Setter
@Entity
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    /** Уникальный идентификатор бронирования. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Дата и время начала бронирования. */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    /** Дата и время окончания бронирования. */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    /** Вещь, которую бронируют. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** Пользователь, который осуществляет бронирование. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    private User booker;

    /** Статус бронирования. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;
}