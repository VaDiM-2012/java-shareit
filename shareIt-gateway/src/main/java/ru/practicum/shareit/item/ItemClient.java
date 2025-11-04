// gateway/src/main/java/ru/practicum/shareit/item/ItemClient.java
package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
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

    // POST /items — создать вещь
    public ResponseEntity<Object> createItem(long userId, ItemRequestDto itemDto) {
        return post("", userId, itemDto);
    }

    // PATCH /items/{itemId} — обновить вещь
    public ResponseEntity<Object> updateItem(long userId, Long itemId, ItemRequestDto itemDto) {
        return patch("/" + itemId, userId, itemDto);
    }

    // GET /items/{itemId} — получить вещь по ID
    public ResponseEntity<Object> getItem(long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    // GET /items — получить все вещи владельца
    public ResponseEntity<Object> getAllByOwner(long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    // GET /items/search — поиск по тексту
    public ResponseEntity<Object> searchItems(long userId, String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }
}