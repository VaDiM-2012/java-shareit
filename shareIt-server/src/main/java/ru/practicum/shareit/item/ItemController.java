package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.validation.CreateGroup;

import java.util.List;

/**
 * Контроллер для управления вещами и комментариями.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    /**
     * Создаёт новую вещь.
     */
    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long ownerId,
                          @Validated(CreateGroup.class) @RequestBody ItemDto itemDto) {
        log.info("Вызван метод создания вещи: " +
                        "владелец (ID) = {}, " +
                        "название = '{}', " +
                        "описание = '{}', " +
                        "доступна для аренды = {}",
                ownerId, itemDto.name(), itemDto.description(), itemDto.available());
        return itemService.create(ownerId, itemDto);
    }

    /**
     * Обновляет существующую вещь.
     */
    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @Validated @RequestBody ItemDto itemDto) {
        log.info("Вызван метод обновления вещи: " +
                        "владелец (ID) = {}, " +
                        "ID вещи = {}, " +
                        "поля для обновления — название = '{}', " +
                        "описание = '{}', " +
                        "доступность = {}",
                ownerId, itemId, itemDto.name(), itemDto.description(), itemDto.available());
        return itemService.update(ownerId, itemId, itemDto);
    }

    /**
     * Получает вещь по ID.
     */
    @GetMapping("/{itemId}")
    public ItemResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                   @PathVariable Long itemId) {
        log.info("Вызван метод получения вещи по ID: " +
                        "пользователь (ID) = {}, " +
                        "ID запрашиваемой вещи = {}",
                userId, itemId);
        return itemService.getById(userId, itemId);
    }

    /**
     * Получает все вещи владельца с пагинацией.
     */
    @GetMapping
    public List<ItemResponseDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                               @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                               @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Вызван метод получения всех вещей владельца: " +
                        "владелец (ID) = {}, " +
                        "пагинация: смещение = {}, размер страницы = {}",
                ownerId, from, size);
        return itemService.getAllByOwner(ownerId, from, size);
    }

    /**
     * Ищет вещи по текстовому запросу с пагинацией.
     */
    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Вызван метод поиска вещей по тексту: " +
                        "поисковый запрос = '{}', " +
                        "пагинация: смещение = {}, размер страницы = {}",
                text, from, size);
        return itemService.search(text, from, size);
    }

    /**
     * Добавляет комментарий к вещи.
     */
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long authorId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentCreateDto dto) {
        log.info("Вызван метод добавления комментария: " +
                        "автор (ID) = {}, " +
                        "ID вещи = {}, " +
                        "текст комментария = '{}'",
                authorId, itemId, dto.text());
        return itemService.addComment(authorId, itemId, dto);
    }
}