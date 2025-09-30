package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.*;

/**
 * In-memory репозиторий для сущности User.
 * Имитирует работу с базой данных, храня все данные в памяти.
 */
@Repository
@Slf4j
public class InMemoryUserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    /**
     * Сохраняет нового пользователя или обновляет существующего.
     * Включает проверку на уникальность и непустоту email.
     *
     * @param user Объект User для сохранения/обновления.
     * @return Сохраненный или обновленный объект User.
     * @throws ValidationException Если email пустой или не уникален.
     */
    public User save(User user) {
        log.info("Попытка сохранения пользователя: {}", user);
        validateEmail(user);

        if (user.getId() == null) {
            user.setId(nextId++);
        }
        users.put(user.getId(), user);
        log.info("Пользователь успешно сохранён: {}", user);
        return user;
    }

    /**
     * Возвращает пользователя по его ID.
     *
     * @param id ID пользователя.
     * @return Optional, содержащий User, если найден.
     */
    public User findById(Long id) {
        log.info("Попытка получения пользователя с ID: {}", id);
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
        return users.get(id);
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return List всех User.
     */
    public List<User> findAll() {
        log.info("Получение списка всех пользователей");
        return new ArrayList<>(users.values());
    }

    /**
     * Удаляет пользователя по его ID.
     *
     * @param id ID пользователя для удаления.
     * @throws ValidationException Если пользователь с указанным ID не найден.
     */
    public void deleteById(Long id) {
        log.info("Попытка удаления пользователя с ID: {}", id);
        if (!users.containsKey(id)) {
            throw new ValidationException("Пользователь с ID " + id + " не найден.");
        }
        users.remove(id);
        log.info("Пользователь с ID {} успешно удалён", id);
    }

    /**
     * Проверяет уникальность и наличие email.
     *
     * @param user Пользователь для проверки.
     * @throws ValidationException Если email пустой или не уникален.
     */
    private void validateEmail(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым.");
        }

        boolean emailExists = users.values().stream()
                .anyMatch(u -> u.getEmail().equals(email) && !Objects.equals(u.getId(), user.getId()));

        if (emailExists) {
            throw new ValidationException("Нарушение уникальности: email уже существует");
        }
    }
}