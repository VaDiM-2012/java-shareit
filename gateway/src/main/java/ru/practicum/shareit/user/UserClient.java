package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserRequestDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Создает нового пользователя.
     *
     * @param userDto данные пользователя.
     * @return ответ с созданным пользователем.
     */
    public ResponseEntity<Object> createUser(UserRequestDto userDto) {
        return post("", userDto);
    }

    /**
     * Обновляет пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param userDto данные для обновления.
     * @return ответ с обновленным пользователем.
     */
    public ResponseEntity<Object> updateUser(Long userId, UserRequestDto userDto) {
        return patch("/" + userId, userDto);
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @return ответ с данными пользователя.
     */
    public ResponseEntity<Object> getUser(Long userId) {
        return get("/" + userId);
    }

    /**
     * Получает список всех пользователей.
     *
     * @return ответ с списком пользователей.
     */
    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя.
     * @return ответ об успешном удалении.
     */
    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete("/" + userId);
    }
}