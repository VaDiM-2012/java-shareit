package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory репозиторий для сущности Item.
 * Хранит данные в HashMap и имитирует работу с базой данных.
 */
@Repository
public class InMemoryItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1L;

    /**
     * Сохраняет нового Item или обновляет существующий.
     *
     * @param item Объект Item для сохранения/обновления.
     * @return Сохраненный или обновленный объект Item.
     */
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    /**
     * Возвращает Item по его ID.
     *
     * @param id ID вещи.
     * @return Optional, содержащий Item, если найден.
     */
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    /**
     * Возвращает список всех вещей, принадлежащих указанному владельцу.
     *
     * @param ownerId ID владельца.
     * @return List всех Item, принадлежащих владельцу.
     */
    public List<Item> findByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    /**
     * Ищет вещи по тексту в названии или описании.
     * Поиск регистронезависимый и только среди доступных вещей.
     *
     * @param text Текст для поиска.
     * @return List найденных Item. Пустой список, если text пустой или не найден.
     */
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        final String searchText = text.trim().toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable) // Только доступные вещи
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
    }
}