package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
 * Реализация сервиса для работы с сущностями {@link Item} и {@link Comment}.
 * Предоставляет методы для создания, обновления, получения и поиска вещей, а также добавления комментариев.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository requestRepository;

    /**
     * Создает новую вещь для указанного владельца.
     */
    @Transactional
    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        log.info("Создание вещи: владелец (ID) = {}, название = '{}', описание = '{}', доступна = {}, ID запроса = {}",
                ownerId, itemDto.name(), itemDto.description(), itemDto.available(), itemDto.requestId());

        User owner = findUserById(ownerId);
        Item item = ItemMapper.toEntity(itemDto);
        item.setOwner(owner);

        if (itemDto.requestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.requestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.requestId() + " не найден."));
            item.setRequest(request);
            log.info("Привязка вещи к запросу: ID запроса = {}", itemDto.requestId());
        }

        Item savedItem = itemRepository.save(item);
        log.info("Вещь успешно создана: ID = {}, название = '{}', владелец = {}", savedItem.getId(), savedItem.getName(), ownerId);
        return ItemMapper.toDto(savedItem);
    }

    /**
     * Обновляет данные вещи для указанного владельца.
     */
    @Transactional
    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи: владелец (ID) = {}, ID вещи = {}, новые данные — название = '{}', описание = '{}', доступна = {}",
                ownerId, itemId, itemDto.name(), itemDto.description(), itemDto.available());

        findUserById(ownerId);
        Item item = findItemById(itemId);

        if (!item.getOwner().getId().equals(ownerId)) {
            log.warn("Попытка обновления вещи пользователем, не являющимся владельцем: пользователь (ID) = {}, владелец вещи = {}",
                    ownerId, item.getOwner().getId());
            throw new NotFoundException("Пользователь ID " + ownerId + " не является владельцем вещи ID " + itemId);
        }

        updateItemFields(item, itemDto);
        Item updatedItem = itemRepository.save(item);
        log.info("Вещь успешно обновлена: ID = {}, название = '{}', владелец = {}", updatedItem.getId(), updatedItem.getName(), ownerId);
        return ItemMapper.toDto(updatedItem);
    }

    /**
     * Получает информацию о вещи по её идентификатору.
     */
    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        log.info("Получение вещи по ID: пользователь (ID) = {}, ID вещи = {}", userId, itemId);

        Item item = findItemById(itemId);
        List<CommentDto> comments = getCommentsByItemId(itemId);

        if (item.getOwner().getId().equals(userId)) {
            log.info("Пользователь — владелец вещи. Добавление данных о бронированиях.");
            return getItemResponseDtoWithBookings(item, comments);
        }

        log.info("Пользователь не владелец. Возвращение базовой информации о вещи.");
        return ItemMapper.toDto(item, null, null, comments);
    }

    /**
     * Получает список всех вещей владельца с пагинацией.
     */
    @Override
    public List<ItemResponseDto> getAllByOwner(Long ownerId, int from, int size) {
        log.info("Получение всех вещей владельца: владелец (ID) = {}, пагинация — смещение = {}, размер = {}", ownerId, from, size);

        findUserById(ownerId);
        PageRequest page = createPageRequest(from, size);

        List<Item> items = itemRepository.findAllByOwnerIdOrderById(ownerId, page);
        if (items.isEmpty()) {
            log.info("У владельца (ID {}) нет вещей.", ownerId);
            return Collections.emptyList();
        }

        Map<Long, List<CommentDto>> commentsMap = getCommentsByItemIds(items);
        List<ItemResponseDto> dtos = items.stream()
                .map(item -> getItemResponseDtoWithBookings(
                        item, commentsMap.getOrDefault(item.getId(), Collections.emptyList())))
                .toList();

        log.info("Возвращено {} вещей для владельца (ID {}).", dtos.size(), ownerId);
        return dtos;
    }

    /**
     * Выполняет поиск вещей по тексту в названии или описании.
     */
    @Override
    public List<ItemDto> search(String text, int from, int size) {
        log.info("Поиск вещей: текст = '{}', пагинация — смещение = {}, размер = {}", text, from, size);

        if (text.isBlank()) {
            log.info("Поисковый запрос пустой. Возвращён пустой список.");
            return Collections.emptyList();
        }

        PageRequest page = createPageRequest(from, size);
        List<Item> items = itemRepository.search(text, page);
        log.info("По запросу '{}' найдено {} вещей.", text, items.size());
        return ItemMapper.toDto(items);
    }

    /**
     * Добавляет комментарий к вещи от имени пользователя.
     */
    @Transactional
    @Override
    public CommentDto addComment(Long authorId, Long itemId, CommentCreateDto commentDto) {
        log.info("Добавление комментария: автор (ID) = {}, ID вещи = {}, текст = '{}'", authorId, itemId, commentDto.text());

        User author = findUserById(authorId);
        Item item = findItemById(itemId);
        validateBookingForComment(authorId, itemId);

        Comment comment = CommentMapper.toEntity(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий успешно добавлен: ID = {}, автор = {}, вещь = {}", savedComment.getId(), authorId, itemId);
        return CommentMapper.toDto(savedComment);
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
     * Находит вещь по ID.
     */
    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с ID {} не найдена.", itemId);
                    return new NotFoundException("Вещь ID " + itemId + " не найдена.");
                });
    }

    /**
     * Получает комментарии для одной вещи.
     */
    private List<CommentDto> getCommentsByItemId(Long itemId) {
        List<CommentDto> comments = CommentMapper.toDto(commentRepository.findAllByItemId(itemId));
        log.debug("Загружено {} комментариев для вещи (ID {}).", comments.size(), itemId);
        return comments;
    }

    /**
     * Получает комментарии для списка вещей.
     */
    private Map<Long, List<CommentDto>> getCommentsByItemIds(List<Item> items) {
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        log.debug("Загрузка комментариев для вещей: {}", itemIds);
        List<Comment> comments = commentRepository.findAllByItemIdIn(itemIds);
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.groupingBy(CommentDto::id));
    }

    /**
     * Проверяет, что пользователь бронировал вещь и бронирование завершено.
     */
    private void validateBookingForComment(Long authorId, Long itemId) {
        log.info("Проверка возможности добавления комментария: пользователь (ID) = {}, вещь (ID) = {}", authorId, itemId);

        List<Booking> pastBookings = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, authorId, BookingStatus.APPROVED, LocalDateTime.now());

        if (pastBookings.isEmpty()) {
            log.warn("Пользователь (ID {}) не имеет завершённых бронирований вещи (ID {}).", authorId, itemId);
            throw new BookingNotFoundException(
                    "Пользователь ID " + authorId + " не бронировал вещь ID " + itemId + " или бронирование не завершено.");
        }

        log.info("Пользователь (ID {}) имеет {} завершённых бронирований вещи (ID {}). Разрешено добавление комментария.",
                authorId, pastBookings.size(), itemId);
    }

    /**
     * Обновляет поля вещи на основе DTO.
     */
    private void updateItemFields(Item item, ItemDto itemDto) {
        boolean updated = false;

        if (itemDto.name() != null && !itemDto.name().isBlank()) {
            item.setName(itemDto.name());
            log.debug("Обновлено название вещи (ID {}): '{}'", item.getId(), itemDto.name());
            updated = true;
        }

        if (itemDto.description() != null && !itemDto.description().isBlank()) {
            item.setDescription(itemDto.description());
            log.debug("Обновлено описание вещи (ID {}).", item.getId());
            updated = true;
        }

        if (itemDto.available() != null) {
            item.setAvailable(itemDto.available());
            log.debug("Обновлён статус доступности вещи (ID {}): {}", item.getId(), itemDto.available());
            updated = true;
        }

        if (!updated) {
            log.debug("Нет данных для обновления вещи (ID {}).", item.getId());
        }
    }

    /**
     * Формирует DTO вещи с данными о бронированиях (для владельца).
     */
    private ItemResponseDto getItemResponseDtoWithBookings(Item item, List<CommentDto> comments) {
        LocalDateTime now = LocalDateTime.now();
        BookingInItemDto lastBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(item.getId(), BookingStatus.APPROVED, now)
                .map(b -> new BookingInItemDto(b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd()))
                .orElse(null);

        BookingInItemDto nextBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                .map(b -> new BookingInItemDto(b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd()))
                .orElse(null);

        log.debug("Сформирован ответ для вещи (ID {}) с бронированиями: последнее = {}, следующее = {}, комментариев = {}",
                item.getId(), lastBooking != null ? lastBooking.id() : "отсутствует",
                nextBooking != null ? nextBooking.id() : "отсутствует", comments.size());

        return ItemMapper.toDto(item, lastBooking, nextBooking, comments);
    }
}