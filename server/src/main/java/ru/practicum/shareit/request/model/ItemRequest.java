package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;

/**
 * Модель данных Запрос на вещь (ItemRequest).
 */
@Getter
@Setter
@Entity
@Table(name = "requests")
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {

    /** Уникальный идентификатор запроса. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Содержание запроса. */
    @Column(name = "description", nullable = false)
    private String description;

    /** Пользователь, создавший запрос. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor;

    /** Дата и время создания запроса. */
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}