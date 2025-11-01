package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.request.model.ItemRequest;

/**
 * Модель данных Вещь (Item).
 */
@Getter
@Setter
@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    /**
     * Уникальный идентификатор вещи.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название вещи.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Описание вещи.
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Доступность вещи для аренды.
     */
    @Column(name = "is_available", nullable = false)
    private Boolean available;

    /**
     * Владелец вещи.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Ссылка на запрос, если вещь была создана в ответ на запрос.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private ItemRequest request;
}