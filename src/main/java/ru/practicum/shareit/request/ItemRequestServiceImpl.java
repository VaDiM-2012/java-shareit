package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.item.ItemMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Имплементация сервиса для работы с сущностью {@link ItemRequest}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ItemRequestResponseDto create(Long requestorId, ItemRequestCreateDto dto) {
        User requestor = findUserById(requestorId);

        ItemRequest request = ItemRequestMapper.toEntity(dto);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Создан запрос ID {} пользователем {}", savedRequest.getId(), requestorId);

        // Возвращаем DTO с пустым списком вещей
        return ItemRequestMapper.toDto(savedRequest, Collections.emptyMap());
    }

    @Override
    public List<ItemRequestResponseDto> getAllByRequestor(Long requestorId) {
        findUserById(requestorId);

        List<ItemRequest> requests = requestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId);
        return mapRequestsToDtoWithItems(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAll(Long userId, int from, int size) {
        findUserById(userId);
        PageRequest page = PageRequest.of(from / size, size);

        // Получаем все запросы, кроме запросов текущего пользователя
        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, page);
        return mapRequestsToDtoWithItems(requests);
    }

    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        findUserById(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден."));

        log.info("Получен запрос ID {} пользователем {}", requestId, userId);
        return mapRequestsToDtoWithItems(List.of(request)).get(0);
    }

    /**
     * Вспомогательный метод для маппинга списка запросов в DTO, включая связанные вещи.
     */
    private List<ItemRequestResponseDto> mapRequestsToDtoWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Собираем все ID запросов
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        // 2. Получаем все вещи, связанные с этими запросами
        Map<Long, List<ItemDto>> itemsMap = itemRepository.findAll().stream()
                .filter(item -> item.getRequest() != null && requestIds.contains(item.getRequest().getId()))
                .map(ItemMapper::toDto)
                .collect(Collectors.groupingBy(ItemDto::requestId));

        // 3. Маппим запросы в DTO
        return ItemRequestMapper.toDto(requests, itemsMap);
    }

    /**
     * Вспомогательный метод для поиска пользователя и обработки NotFound.
     * @param userId ID пользователя.
     * @return Объект User.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
    }
}