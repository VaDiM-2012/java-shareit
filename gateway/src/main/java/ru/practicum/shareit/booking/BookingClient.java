package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    /**
     * Получает список бронирований пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param state состояние бронирований.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком бронирований.
     */
    public ResponseEntity<Object> getBookings(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }

    /**
     * Создает новое бронирование вещи.
     *
     * @param userId идентификатор пользователя.
     * @param requestDto данные для бронирования.
     * @return ответ с созданным бронированием.
     */
    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    /**
     * Получает информацию о бронировании по идентификатору.
     *
     * @param userId идентификатор пользователя.
     * @param bookingId идентификатор бронирования.
     * @return ответ с данными бронирования.
     */
    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    /**
     * Одобряет или отклоняет бронирование.
     *
     * @param userId идентификатор пользователя.
     * @param bookingId идентификатор бронирования.
     * @param approved флаг одобрения.
     * @return ответ с обновленным бронированием.
     */
    public ResponseEntity<Object> approveOrReject(long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    /**
     * Получает список бронирований владельца.
     *
     * @param userId идентификатор владельца.
     * @param state состояние бронирований.
     * @param from начальный индекс для пагинации.
     * @param size количество элементов на странице.
     * @return ответ с списком бронирований.
     */
    public ResponseEntity<Object> getAllByOwner(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "from", from,
                "size", size
        );
        return get("/owner?state={state}&from={from}&size={size}", userId, parameters);
    }
}