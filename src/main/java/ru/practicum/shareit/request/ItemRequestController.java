package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;


import java.util.List;

/**
 * REST-контроллер для обработки запросов, связанных с сущностью ItemRequest.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    /**
     * POST /requests - Создание нового запроса на вещь.
     *
     * @param userId ID пользователя, создающего запрос.
     * @param dto DTO с данными запроса.
     * @return DTO созданного запроса.
     * @throws UnsupportedOperationException .
     */
    @PostMapping
    public ItemRequestDto createRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemRequestDto dto) {
        log.info("Endpoint POST /requests: Request to create item request by user {}", userId);
        return itemRequestService.createRequest(userId, dto);
    }

    /**
     * GET /requests/{requestId} - Получение данных о конкретном запросе.
     *
     * @param requestId ID запроса.
     * @param userId ID пользователя, запрашивающего данные.
     * @return DTO найденного запроса.
     * @throws UnsupportedOperationException .
     */
    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Endpoint GET /requests/{}: Request to get item request by user {}", requestId, userId);
        return itemRequestService.getRequestById(requestId, userId);
    }

    /**
     * GET /requests/all - Получение списка всех запросов, созданных другими пользователями.
     *
     * @param userId ID пользователя, запрашивающего список.
     * @param from Начальный индекс.
     * @param size Количество элементов.
     * @return Список DTO запросов.
     * @throws UnsupportedOperationException .
     */
    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Endpoint GET /requests/all: Request to get all item requests (from={}, size={}) by user {}", from, size, userId);
        return itemRequestService.getAllRequests(userId, from, size);
    }

    /**
     * GET /requests - Получение списка всех запросов, созданных текущим пользователем.
     *
     * @param userId ID пользователя-запросчика.
     * @return Список DTO запросов.
     * @throws UnsupportedOperationException .
     */
    @GetMapping
    public List<ItemRequestDto> getAllRequestsByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Endpoint GET /requests: Request to get user's item requests by user {}", userId);
        return itemRequestService.getAllRequestsByUser(userId);
    }
}