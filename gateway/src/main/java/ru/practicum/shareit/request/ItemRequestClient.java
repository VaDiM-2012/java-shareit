package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Создает запрос на вещь.
     *
     * @param userId идентификатор пользователя.
     * @param requestDto данные запроса.
     * @return ответ с созданным запросом.
     */
    public ResponseEntity<Object> createRequest(long userId, ItemRequestRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    /**
     * Получает список собственных запросов.
     *
     * @param userId идентификатор пользователя.
     * @return ответ с списком запросов.
     */
    public ResponseEntity<Object> getOwnRequests(long userId) {
        return get("", userId);
    }

    /**
     * Получает список всех запросов с пагинацией.
     *
     * @param userId идентификатор пользователя.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком запросов.
     */
    public ResponseEntity<Object> getAllRequests(long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&size={size}", userId, parameters);
    }

    /**
     * Получает запрос по идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @param requestId идентификатор запроса.
     * @return ответ с данными запроса.
     */
    public ResponseEntity<Object> getRequestById(long userId, Long requestId) {
        return get("/" + requestId, userId);
    }
}