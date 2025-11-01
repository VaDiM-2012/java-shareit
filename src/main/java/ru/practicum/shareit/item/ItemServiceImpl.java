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
     *
     * @param ownerId Идентификатор владельца вещи.
     * @param itemDto Данные вещи для создания.
     * @return DTO созданной вещи {@link ItemDto}.
     * @throws NotFoundException если пользователь или запрос (если указан) не найдены.
     */
    @Transactional
    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = findUserById(ownerId);
        Item item = ItemMapper.toEntity(itemDto);
        item.setOwner(owner);

        if (itemDto.requestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.requestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.requestId() + " не найден."));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        log.info("Пользователь ID {} создал вещь: {}", ownerId, savedItem.getName());
        return ItemMapper.toDto(savedItem);
    }

    /**
     * Обновляет данные вещи для указанного владельца.
     *
     * @param ownerId Идентификатор владельца вещи.
     * @param itemId  Идентификатор вещи.
     * @param itemDto Данные для обновления вещи.
     * @return DTO обновленной вещи {@link ItemDto}.
     * @throws NotFoundException если пользователь или вещь не найдены, или пользователь не является владельцем.
     */
    @Transactional
    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        findUserById(ownerId);
        Item item = findItemById(itemId);

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь ID " + ownerId + " не является владельцем вещи ID " + itemId);
        }

        updateItemFields(item, itemDto);
        Item updatedItem = itemRepository.save(item);
        log.info("Пользователь ID {} обновил вещь: {}", ownerId, updatedItem.getName());
        return ItemMapper.toDto(updatedItem);
    }

    /**
     * Получает информацию о вещи по её идентификатору.
     * Для владельца включает данные о последнем и следующем бронировании.
     *
     * @param userId Идентификатор пользователя, запрашивающего информацию.
     * @param itemId Идентификатор вещи.
     * @return DTO вещи {@link ItemResponseDto} с комментариями и, если пользователь — владелец, данными о бронированиях.
     * @throws NotFoundException если вещь не найдена.
     */
    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = findItemById(itemId);
        List<CommentDto> comments = getCommentsByItemId(itemId);

        if (item.getOwner().getId().equals(userId)) {
            return getItemResponseDtoWithBookings(item, comments);
        }

        log.info("Получена вещь ID {} пользователем ID {}", itemId, userId);
        return ItemMapper.toDto(item, null, null, comments);
    }

    /**
     * Получает список всех вещей владельца с пагинацией.
     * Для каждой вещи включает данные о последнем и следующем бронировании, а также комментарии.
     *
     * @param ownerId Идентификатор владельца.
     * @param from    Индекс первого элемента (для пагинации).
     * @param size    Количество элементов на странице.
     * @return Список DTO вещей {@link ItemResponseDto}.
     * @throws NotFoundException если пользователь не найден.
     */
    @Override
    public List<ItemResponseDto> getAllByOwner(Long ownerId, int from, int size) {
        findUserById(ownerId);
        PageRequest page = createPageRequest(from, size);

        List<Item> items = itemRepository.findAllByOwnerIdOrderById(ownerId, page);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<CommentDto>> commentsMap = getCommentsByItemIds(items);
        List<ItemResponseDto> dtos = items.stream()
                .map(item -> getItemResponseDtoWithBookings(
                        item, commentsMap.getOrDefault(item.getId(), Collections.emptyList())))
                .toList();

        log.info("Получен список вещей владельца ID {}. Количество: {}", ownerId, dtos.size());
        return dtos;
    }

    /**
     * Выполняет поиск вещей по тексту в названии или описании с пагинацией.
     * Возвращает только доступные вещи.
     *
     * @param text Текст для поиска.
     * @param from Индекс первого элемента (для пагинации).
     * @param size Количество элементов на странице.
     * @return Список DTO вещей {@link ItemDto}.
     */
    @Override
    public List<ItemDto> search(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        PageRequest page = createPageRequest(from, size);
        List<Item> items = itemRepository.search(text, page);
        log.info("Поиск по тексту '{}'. Найдено: {}", text, items.size());
        return ItemMapper.toDto(items);
    }

    /**
     * Добавляет комментарий к вещи от имени пользователя.
     * Проверяет, что пользователь бронировал вещь и бронирование завершено.
     *
     * @param authorId   Идентификатор автора комментария.
     * @param itemId     Идентификатор вещи.
     * @param commentDto Данные комментария.
     * @return DTO созданного комментария {@link CommentDto}.
     * @throws NotFoundException         если пользователь или вещь не найдены.
     * @throws BookingNotFoundException если пользователь не бронировал вещь или бронирование не завершено.
     */
    @Transactional
    @Override
    public CommentDto addComment(Long authorId, Long itemId, CommentCreateDto commentDto) {
        User author = findUserById(authorId);
        Item item = findItemById(itemId);
        validateBookingForComment(authorId, itemId);

        Comment comment = CommentMapper.toEntity(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.info("Пользователь ID {} добавил комментарий к вещи ID {}", authorId, itemId);
        return CommentMapper.toDto(savedComment);
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
     * Получает вещь по идентификатору.
     *
     * @param itemId Идентификатор вещи.
     * @return Объект {@link Item}.
     * @throws NotFoundException если вещь не найдена.
     */
    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь ID " + itemId + " не найдена."));
    }

    /**
     * Получает список комментариев для вещи по её идентификатору.
     *
     * @param itemId Идентификатор вещи.
     * @return Список DTO комментариев {@link CommentDto}.
     */
    private List<CommentDto> getCommentsByItemId(Long itemId) {
        return CommentMapper.toDto(commentRepository.findAllByItemId(itemId));
    }

    /**
     * Получает комментарии для списка вещей, сгруппированные по идентификатору вещи.
     *
     * @param items Список вещей.
     * @return Карта, где ключ — идентификатор вещи, значение — список DTO комментариев.
     */
    private Map<Long, List<CommentDto>> getCommentsByItemIds(List<Item> items) {
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        return commentRepository.findAllByItemIdIn(itemIds)
                .stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.groupingBy(CommentDto::id));
    }

    /**
     * Проверяет, что пользователь бронировал вещь и бронирование завершено.
     *
     * @param authorId Идентификатор пользователя.
     * @param itemId   Идентификатор вещи.
     * @throws BookingNotFoundException если бронирование не найдено или не завершено.
     */
    private void validateBookingForComment(Long authorId, Long itemId) {
        List<Booking> pastBookings = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, authorId, BookingStatus.APPROVED, LocalDateTime.now());
        if (pastBookings.isEmpty()) {
            throw new BookingNotFoundException(
                    "Пользователь ID " + authorId + " не бронировал вещь ID " + itemId + " или бронирование не завершено.");
        }
    }

    /**
     * Обновляет поля вещи на основе данных из DTO.
     *
     * @param item    Объект вещи для обновления.
     * @param itemDto DTO с данными для обновления.
     */
    private void updateItemFields(Item item, ItemDto itemDto) {
        if (itemDto.name() != null && !itemDto.name().isBlank()) {
            item.setName(itemDto.name());
        }
        if (itemDto.description() != null && !itemDto.description().isBlank()) {
            item.setDescription(itemDto.description());
        }
        if (itemDto.available() != null) {
            item.setAvailable(itemDto.available());
        }
    }

    /**
     * Формирует DTO вещи с данными о последнем и следующем бронировании для владельца.
     *
     * @param item     Объект вещи.
     * @param comments Список комментариев к вещи.
     * @return DTO вещи {@link ItemResponseDto} с данными о бронированиях и комментариях.
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
        return ItemMapper.toDto(item, lastBooking, nextBooking, comments);
    }
}