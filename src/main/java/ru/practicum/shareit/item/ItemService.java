// 3. Обновлённый ItemService (с новым исключением и устранением дублирования)

package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerMismatchException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления сущностью Item.
 * Содержит бизнес-логику и координацию работы с репозиторием.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final InMemoryItemRepository itemRepository;
    private final UserService userService;

    /**
     * Создает новую вещь.
     *
     * @param itemDto DTO с данными новой вещи.
     * @param userId ID пользователя, который является владельцем.
     * @return DTO созданной вещи.
     * @throws NotFoundException Если владелец с указанным userId не найден.
     */
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        log.info("Начало создания вещи. Пользователь с ID={}, данные вещи={}", userId, itemDto);

        // 1. Проверка существования владельца (User)
        UserDto ownerDto = userService.findById(userId);
        User owner = new User(ownerDto.getId(), ownerDto.getName(), ownerDto.getEmail());

        // 2. Преобразование и сохранение
        Item item = ItemMapper.toItem(itemDto, owner, null);
        Item savedItem = itemRepository.save(item);

        ItemDto result = ItemMapper.toItemDto(savedItem);
        log.info("Завершение создания вещи. Результат: {}", result);
        return result;
    }

    /**
     * Обновляет данные вещи по ее ID. Реализует PATCH-логику (обновление только не-null полей).
     *
     * @param itemId ID вещи для обновления.
     * @param itemDto DTO с обновляемыми данными.
     * @param userId ID пользователя, который пытается обновить вещь (должен быть владельцем).
     * @return DTO обновленной вещи.
     * @throws NotFoundException Если вещь не найдена.
     * @throws OwnerMismatchException Если пользователь не является владельцем вещи.
     */
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        log.info("Начало обновления вещи. ID вещи={}, ID пользователя={}, данные обновления={}", itemId, userId, itemDto);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID=" + itemId + " не найдена."));

        // 1. Проверка, что пользователь является владельцем
        validateOwner(userId, existingItem);

        // 2. Обновление только не-null полей (PATCH-логика)
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        // 3. Сохранение
        Item updatedItem = itemRepository.save(existingItem);

        ItemDto result = ItemMapper.toItemDto(updatedItem);
        log.info("Завершение обновления вещи. Результат: {}", result);
        return result;
    }

    /**
     * Проверяет, является ли пользователь владельцем вещи.
     *
     * @param userId ID пользователя.
     * @param item Вещь для проверки.
     * @throws OwnerMismatchException Если пользователь не является владельцем.
     */
    private void validateOwner(Long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            log.error("Попытка обновления вещи не владельцем. Владелец ID={}, Запрашивающий пользователь ID={}", item.getOwner().getId(), userId);
            throw new OwnerMismatchException("Пользователь с ID=" + userId + " не является владельцем вещи ID=" + item.getId());
        }
    }

    /**
     * Находит вещь по ID.
     *
     * @param itemId ID вещи.
     * @return DTO найденной вещи.
     * @throws NotFoundException Если вещь не найдена.
     */
    public ItemDto getItemById(Long itemId) {
        log.info("Начало получения вещи по ID. ID={}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID=" + itemId + " не найдена."));

        ItemDto result = ItemMapper.toItemDto(item);
        log.info("Завершение получения вещи. Результат: {}", result);
        return result;
    }

    /**
     * Возвращает список всех вещей, принадлежащих конкретному пользователю.
     *
     * @param userId ID владельца.
     * @return List DTO всех вещей владельца.
     */
    public List<ItemDto> getItemsByOwner(Long userId) {
        log.info("Начало получения списка вещей владельца. ID владельца={}", userId);

        userService.findById(userId); // Проверка существования пользователя

        List<ItemDto> result = itemRepository.findByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        log.info("Завершение получения списка вещей. Найдено {} вещей", result.size());
        return result;
    }

    /**
     * Ищет доступные вещи по тексту в названии или описании.
     *
     * @param text Текст для поиска.
     * @return List DTO найденных вещей. Возвращает пустой список, если text пустой.
     */
    public List<ItemDto> searchItems(String text) {
        log.info("Начало поиска вещей. Ключевое слово='{}'", text);

        if (text == null || text.isBlank()) {
            log.info("Завершение поиска. Ключевое слово пустое, возвращаю пустой список");
            return Collections.emptyList();
        }

        List<ItemDto> result = itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        log.info("Завершение поиска. Найдено {} вещей по ключевому слову '{}'", result.size(), text);
        return result;
    }
}