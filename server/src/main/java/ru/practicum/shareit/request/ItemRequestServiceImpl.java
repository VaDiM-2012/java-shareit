package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с сущностью {@link ItemRequest}.
 * Предоставляет методы для создания, получения и просмотра запросов на вещи.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    /**
     * Создаёт новый запрос на вещь от имени пользователя.
     */
    @Transactional
    @Override
    public ItemRequestResponseDto create(Long requestorId, ItemRequestCreateDto requestDto) {
        log.info("Создание запроса на вещь: инициатор (ID) = {}, описание = '{}'", requestorId, requestDto.description());

        User requestor = findUserById(requestorId);
        ItemRequest request = ItemRequestMapper.toEntity(requestDto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Запрос успешно создан: ID = {}, инициатор = {}", savedRequest.getId(), requestorId);
        return ItemRequestMapper.toDto(savedRequest, Collections.emptyMap());
    }

    /**
     * Получает все запросы, созданные пользователем.
     */
    @Override
    public List<ItemRequestResponseDto> getAllByRequestor(Long requestorId) {
        log.info("Получение всех запросов пользователя: инициатор (ID) = {}", requestorId);

        findUserById(requestorId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId);

        if (requests.isEmpty()) {
            log.info("У пользователя (ID {}) нет активных запросов.", requestorId);
            return Collections.emptyList();
        }

        log.info("Найдено {} запросов для пользователя (ID {}).", requests.size(), requestorId);
        return mapToDtoWithItems(requests);
    }

    /**
     * Получает все запросы других пользователей (кроме собственных) с пагинацией.
     */
    @Override
    public List<ItemRequestResponseDto> getAll(Long userId, int from, int size) {
        log.info("Получение всех запросов других пользователей: " +
                        "пользователь (ID) = {}, " +
                        "пагинация: смещение = {}, размер страницы = {}",
                userId, from, size);

        findUserById(userId);
        PageRequest page = createPageRequest(from, size);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, page);

        if (requests.isEmpty()) {
            log.info("Нет доступных запросов для пользователя (ID {}).", userId);
            return Collections.emptyList();
        }

        log.info("Найдено {} запросов, доступных пользователю (ID {}).", requests.size(), userId);
        return mapToDtoWithItems(requests);
    }

    /**
     * Получает запрос по его ID.
     */
    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        log.info("Получение запроса по ID: пользователь (ID) = {}, ID запроса = {}", userId, requestId);

        findUserById(userId);
        ItemRequest request = findRequestById(requestId);

        log.info("Запрос найден: ID = {}, описание = '{}', инициатор = {}",
                request.getId(), request.getDescription(), request.getRequestor().getId());
        return mapToDtoWithItems(List.of(request)).getFirst();
    }

    /**
     * Создаёт объект PageRequest для пагинации.
     */
    private PageRequest createPageRequest(int from, int size) {
        int page = from / size;
        log.debug("Создание PageRequest: страница = {}, размер = {}", page, size);
        return PageRequest.of(page, size);
    }

    /**
     * Находит пользователя по ID.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с ID {} не найден.", userId);
                    return new NotFoundException("Пользователь ID " + userId + " не найден.");
                });
    }

    /**
     * Находит запрос по ID.
     */
    private ItemRequest findRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос с ID {} не найден.", requestId);
                    return new NotFoundException("Запрос ID " + requestId + " не найден.");
                });
    }

    /**
     * Преобразует список запросов в DTO с прикреплёнными вещами.
     */
    private List<ItemRequestResponseDto> mapToDtoWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            log.debug("Список запросов пуст. Возвращён пустой список DTO.");
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        log.debug("Загрузка вещей, связанных с запросами: {}", requestIds);

        Map<Long, List<ItemDto>> itemsMap = itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.groupingBy(ItemDto::requestId));

        log.debug("Найдено вещей по запросам: {} шт.", itemsMap.values().stream().mapToInt(List::size).sum());

        return ItemRequestMapper.toDto(requests, itemsMap);
    }
}