package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Модель данных Пользователь (User).
 */
@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Уникальный идентификатор пользователя. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Имя пользователя. */
    @Column(name = "name", nullable = false)
    private String name;

    /** Электронная почта пользователя (должна быть уникальной). */
    @Column(name = "email", nullable = false, unique = true)
    private String email;
}