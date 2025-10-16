package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

/**
 * REST-контроллер для обработки запросов, связанных с сущностью Item.
 * Основной путь: /items.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    /**
     * POST /items - Создание новой вещи.
     *
     * @param itemDto DTO с данными новой вещи (проходит Bean Validation).
     * @param userId ID пользователя-владельца (из заголовка X-Sharer-User-Id).
     * @return DTO созданной вещи.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Получен запрос POST /items. Пользователь с ID {} создаёт вещь {}", userId, itemDto);
        ItemDto createdItem = itemService.createItem(itemDto, userId);
        log.info("Вещь успешно создана. ID: {}", createdItem.getId());
        return createdItem;
    }

    /**
     * PATCH /items/{itemId} - Обновление существующей вещи.
     *
     * @param itemId ID вещи для обновления.
     * @param itemDto DTO с обновляемыми данными.
     * @param userId ID пользователя, который пытается обновить вещь (из заголовка).
     * @return DTO обновленной вещи.
     */
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("Получен запрос PATCH /items/{}. Пользователь с ID {} обновляет вещь {}", itemId, userId, itemDto);
        ItemDto updatedItem = itemService.updateItem(itemId, itemDto, userId);
        log.info("Вещь с ID {} успешно обновлена", itemId);
        return updatedItem;
    }

    /**
     * GET /items/{itemId} - Получение информации о конкретной вещи.
     *
     * @param itemId ID вещи.
     * @return DTO найденной вещи.
     */
    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        log.info("Получен запрос GET /items/{}. Получение информации о вещи", itemId);
        ItemDto item = itemService.getItemById(itemId);
        log.info("Информация о вещи с ID {} успешно получена", itemId);
        return item;
    }

    /**
     * GET /items - Получение списка всех вещей конкретного пользователя.
     *
     * @param userId ID владельца (из заголовка X-Sharer-User-Id).
     * @return List DTO всех вещей владельца.
     */
    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос GET /items. Получение всех вещей пользователя с ID {}", userId);
        List<ItemDto> items = itemService.getItemsByOwner(userId);
        log.info("Для пользователя с ID {} найдено {} вещей", userId, items.size());
        return items;
    }

    /**
     * GET /items/search - Поиск вещей по названию или описанию.
     *
     * @param text Текст для поиска (из параметра запроса).
     * @return List DTO найденных вещей.
     */
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(required = false) String text) {
        log.info("Получен запрос GET /items/search. Поиск вещей по тексту '{}'", text);
        List<ItemDto> items = itemService.searchItems(text);
        log.info("Найдено {} вещей по запросу '{}'", items.size(), text);
        return items;
    }
}