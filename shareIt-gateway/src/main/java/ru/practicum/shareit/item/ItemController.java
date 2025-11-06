package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.validation.CreateGroup;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    /**
     * Создает новую вещь.
     *
     * @param ownerId идентификатор владельца.
     * @param itemDto данные вещи.
     * @return ответ с созданной вещью.
     */
    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                         @Validated(CreateGroup.class) @RequestBody  ItemRequestDto itemDto) {
        log.info("POST /items: Создание вещи пользователем {}", ownerId);
        return itemClient.createItem(ownerId, itemDto);
    }

    /**
     * Обновляет вещь.
     *
     * @param ownerId идентификатор владельца.
     * @param itemId идентификатор вещи.
     * @param itemDto данные для обновления.
     * @return ответ с обновленной вещью.
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                         @PathVariable Long itemId,
                                         @Validated @RequestBody ItemRequestDto itemDto) {
        log.info("PATCH /items/{}: Обновление вещи пользователем {}", itemId, ownerId);
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }

    /**
     * Получает вещь по идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @param itemId идентификатор вещи.
     * @return ответ с данными вещи.
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long itemId) {
        log.info("GET /items/{}: Получение вещи пользователем {}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    /**
     * Получает список вещей владельца.
     *
     * @param ownerId идентификатор владельца.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком вещей.
     */
    @GetMapping
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                                @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /items: Получение всех вещей владельца {}, from={}, size={}", ownerId, from, size);
        return itemClient.getAllByOwner(ownerId, from, size);
    }

    /**
     * Ищет вещи по тексту.
     *
     * @param text текст для поиска.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком найденных вещей.
     */
    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text,
                                         @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                         @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /items/search?text={}: Поиск вещей", text);
        return itemClient.searchItems(text, from, size);
    }

    /**
     * Добавляет комментарий к вещи.
     *
     * @param authorId идентификатор автора.
     * @param itemId идентификатор вещи.
     * @param commentDto данные комментария.
     * @return ответ с добавленным комментарием.
     */
    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) Long authorId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {

        log.info("POST /items/{}/comment: Добавление комментария пользователем {}", itemId, authorId);

        return itemClient.addComment(authorId, itemId, commentDto);
    }
}