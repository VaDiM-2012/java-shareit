package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collections;
import java.util.List;

/**
 * Сервис для управления сущностью ItemRequest.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestService {

    /**
     * Создает новый запрос на вещь.
     *
     * @param userId ID пользователя, создающего запрос (requestor).
     * @param dto DTO с данными запроса.
     * @return DTO созданного запроса.
     * @throws UnsupportedOperationException .
     */
    public ItemRequestDto createRequest(Long userId, ItemRequestDto dto) {
        log.info("Получен запрос на создание ItemRequest от пользователя {} с данными: {}", userId, dto);
        throw new UnsupportedOperationException("");
    }

    /**
     * Возвращает конкретный запрос по ID.
     *
     * @param requestId ID запроса.
     * @param userId ID пользователя (для проверки доступа).
     * @return DTO найденного запроса.
     * @throws UnsupportedOperationException .
     */
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.info("Получен запрос на получение ItemRequest {} пользователем {}", requestId, userId);
        throw new UnsupportedOperationException("");
    }

    /**
     * Возвращает список всех запросов, созданных конкретным пользователем.
     *
     * @param userId ID пользователя-запросчика.
     * @return Список DTO запросов.
     * @throws UnsupportedOperationException .
     */
    public List<ItemRequestDto> getAllRequestsByUser(Long userId) {
        log.info("Получен запрос на получение списка ItemRequest для пользователя {}", userId);
        return Collections.emptyList();
    }

    /**
     * Возвращает список всех запросов, созданных другими пользователями (с пагинацией).
     *
     * @param userId ID пользователя, запрашивающего список.
     * @param from Начальный индекс.
     * @param size Количество элементов.
     * @return Список DTO запросов.
     * @throws UnsupportedOperationException .
     */
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Получен запрос на получение всех ItemRequest (from={}, size={}) пользователем {}", from, size, userId);
        return Collections.emptyList();
    }
}