package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
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

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long ownerId,
                          @Valid @RequestBody ItemDto itemDto) {
        log.info("POST /items (Owner: {}): Создание вещи", ownerId);
        return itemService.create(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{} (Owner: {}): Обновление вещи", itemId, ownerId);
        return itemService.update(ownerId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                   @PathVariable Long itemId) {
        log.info("GET /items/{} (User: {}): Получение вещи", itemId, userId);
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemResponseDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                               @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                               @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /items (Owner: {}): Получение всех вещей владельца", ownerId);
        return itemService.getAllByOwner(ownerId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /items/search: Поиск вещей по тексту '{}'", text);
        return itemService.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long authorId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentCreateDto dto) {
        log.info("POST /items/{}/comment (Author: {}): Добавление комментария", itemId, authorId);
        return itemService.addComment(authorId, itemId, dto);
    }
}