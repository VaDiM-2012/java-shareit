package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Создает новую вещь.
     *
     * @param userId идентификатор пользователя.
     * @param itemDto данные вещи.
     * @return ответ с созданной вещью.
     */
    public ResponseEntity<Object> createItem(long userId, ItemRequestDto itemDto) {
        return post("", userId, itemDto);
    }

    /**
     * Обновляет вещь.
     *
     * @param userId идентификатор пользователя.
     * @param itemId идентификатор вещи.
     * @param itemDto данные для обновления.
     * @return ответ с обновленной вещью.
     */
    public ResponseEntity<Object> updateItem(long userId, Long itemId, ItemRequestDto itemDto) {
        return patch("/" + itemId, userId, itemDto);
    }

    /**
     * Получает вещь по идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @param itemId идентификатор вещи.
     * @return ответ с данными вещи.
     */
    public ResponseEntity<Object> getItem(long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    /**
     * Получает список вещей владельца.
     *
     * @param userId идентификатор владельца.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком вещей.
     */
    public ResponseEntity<Object> getAllByOwner(long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    /**
     * Ищет вещи по тексту.
     *
     * @param text текст для поиска.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком найденных вещей.
     */
    public ResponseEntity<Object> searchItems(String text, Integer from, Integer size) {

        String path = "/search?text=" + text + "&from=" + from + "&size=" + size;

        return get(path);
    }

    /**
     * Добавляет комментарий к вещи.
     *
     * @param userId идентификатор пользователя.
     * @param itemId идентификатор вещи.
     * @param commentDto данные комментария.
     * @return ответ с добавленным комментарием.
     */
    public ResponseEntity<Object> addComment(long userId, long itemId, CommentDto commentDto) {
        String path = String.format("/%d/comment", itemId);
        return post(path, userId, commentDto);
    }
}