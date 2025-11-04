package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import java.util.List;

/**
 * Контроллер для управления запросами на вещи.
 */
@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestResponseDto create(@RequestHeader(USER_ID_HEADER) Long requestorId,
                                         @Valid @RequestBody ItemRequestCreateDto dto) {
        log.info("POST /requests (Requestor: {}): Создание запроса", requestorId);
        return requestService.create(requestorId, dto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getAllByRequestor(@RequestHeader(USER_ID_HEADER) Long requestorId) {
        log.info("GET /requests (Requestor: {}): Получение своих запросов", requestorId);
        return requestService.getAllByRequestor(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAll(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                               @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /requests/all (User: {}): Получение всех запросов, кроме своих", userId);
        return requestService.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long requestId) {
        log.info("GET /requests/{} (User: {}): Получение запроса", requestId, userId);
        return requestService.getById(userId, requestId);
    }
}