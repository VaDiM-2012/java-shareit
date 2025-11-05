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

    /**
     * Создаёт новый запрос на вещь от имени пользователя.
     */
    @PostMapping
    public ItemRequestResponseDto create(@RequestHeader(USER_ID_HEADER) Long requestorId,
                                         @Valid @RequestBody ItemRequestCreateDto dto) {
        log.info("Вызван метод создания запроса на вещь: " +
                        "инициатор (ID) = {}, " +
                        "описание запроса = '{}'",
                requestorId, dto.description());
        return requestService.create(requestorId, dto);
    }

    /**
     * Получает все запросы, созданные пользователем.
     */
    @GetMapping
    public List<ItemRequestResponseDto> getAllByRequestor(@RequestHeader(USER_ID_HEADER) Long requestorId) {
        log.info("Вызван метод получения всех запросов пользователя: инициатор (ID) = {}", requestorId);
        return requestService.getAllByRequestor(requestorId);
    }

    /**
     * Получает все запросы других пользователей (кроме собственных) с пагинацией.
     */
    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAll(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                               @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Вызван метод получения всех запросов, кроме своих: " +
                        "пользователь (ID) = {}, " +
                        "пагинация: смещение = {}, размер страницы = {}",
                userId, from, size);
        return requestService.getAll(userId, from, size);
    }

    /**
     * Получает запрос по его ID.
     */
    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                          @PathVariable Long requestId) {
        log.info("Вызван метод получения запроса по ID: " +
                        "пользователь (ID) = {}, " +
                        "ID запрашиваемого запроса = {}",
                userId, requestId);
        return requestService.getById(userId, requestId);
    }
}