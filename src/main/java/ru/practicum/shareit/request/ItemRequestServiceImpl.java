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
     * Создает новый запрос на вещь от имени пользователя.
     *
     * @param requestorId Идентификатор пользователя, создающего запрос.
     * @param requestDto  Данные запроса.
     * @return DTO созданного запроса {@link ItemRequestResponseDto}.
     * @throws NotFoundException если пользователь не найден.
     */
    @Transactional
    @Override
    public ItemRequestResponseDto create(Long requestorId, ItemRequestCreateDto requestDto) {
        User requestor = findUserById(requestorId);
        ItemRequest request = ItemRequestMapper.toEntity(requestDto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Пользователь ID {} создал запрос ID {}", requestorId, savedRequest.getId());
        return ItemRequestMapper.toDto(savedRequest, Collections.emptyMap());
    }

    /**
     * Получает список всех запросов, созданных указанным пользователем.
     *
     * @param requestorId Идентификатор пользователя.
     * @return Список DTO запросов {@link ItemRequestResponseDto}.
     * @throws NotFoundException если пользователь не найден.
     */
    @Override
    public List<ItemRequestResponseDto> getAllByRequestor(Long requestorId) {
        findUserById(requestorId);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId);
        return mapToDtoWithItems(requests);
    }

    /**
     * Получает список всех запросов, созданных другими пользователями, с пагинацией.
     *
     * @param userId Идентификатор пользователя, запрашивающего список.
     * @param from   Индекс первого элемента (для пагинации).
     * @param size   Количество элементов на странице.
     * @return Список DTO запросов {@link ItemRequestResponseDto}.
     * @throws NotFoundException если пользователь не найден.
     */
    @Override
    public List<ItemRequestResponseDto> getAll(Long userId, int from, int size) {
        findUserById(userId);
        PageRequest page = createPageRequest(from, size);
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, page);
        return mapToDtoWithItems(requests);
    }

    /**
     * Получает запрос по его идентификатору.
     *
     * @param userId    Идентификатор пользователя, запрашивающего информацию.
     * @param requestId Идентификатор запроса.
     * @return DTO запроса {@link ItemRequestResponseDto}.
     * @throws NotFoundException если пользователь или запрос не найдены.
     */
    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        findUserById(userId);
        ItemRequest request = findRequestById(requestId);
        log.info("Пользователь ID {} получил запрос ID {}", userId, requestId);
        return mapToDtoWithItems(List.of(request)).getFirst();
    }

    /**
     * Создает объект {@link PageRequest} для пагинации на основе индекса и размера страницы.
     *
     * @param from Индекс первого элемента.
     * @param size Количество элементов на странице.
     * @return Объект {@link PageRequest}.
     */
    private PageRequest createPageRequest(int from, int size) {
        return PageRequest.of(from / size, size);
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param userId Идентификатор пользователя.
     * @return Объект {@link User}.
     * @throws NotFoundException если пользователь не найден.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь ID " + userId + " не найден."));
    }

    /**
     * Получает запрос по идентификатору.
     *
     * @param requestId Идентификатор запроса.
     * @return Объект {@link ItemRequest}.
     * @throws NotFoundException если запрос не найден.
     */
    private ItemRequest findRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос ID " + requestId + " не найден."));
    }

    /**
     * Преобразует список запросов в DTO с учетом связанных вещей.
     *
     * @param requests Список запросов.
     * @return Список DTO запросов {@link ItemRequestResponseDto}.
     */
    private List<ItemRequestResponseDto> mapToDtoWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<ItemDto>> itemsMap = itemRepository.findAllByRequestIdIn(requestIds)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.groupingBy(ItemDto::requestId));

        return ItemRequestMapper.toDto(requests, itemsMap);
    }
}