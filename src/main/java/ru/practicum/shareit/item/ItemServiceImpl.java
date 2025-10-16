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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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
 * Имплементация сервиса для работы с сущностями {@link Item} и {@link Comment}.
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

    @Transactional
    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = findUserById(ownerId);
        Item item = ItemMapper.toEntity(itemDto);
        item.setOwner(owner);

        // Установка запроса, если указан request_id
        if (itemDto.requestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.requestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.requestId() + " не найден."));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        log.info("Пользователь {} создал вещь: {}", ownerId, savedItem.getName());
        return ItemMapper.toDto(savedItem);
    }

    @Transactional
    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        findUserById(ownerId); // Проверка существования пользователя

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена."));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь с ID " + ownerId + " не является владельцем вещи.");
        }

        if (itemDto.name() != null) {
            existingItem.setName(itemDto.name());
        }
        if (itemDto.description() != null) {
            existingItem.setDescription(itemDto.description());
        }
        if (itemDto.available() != null) {
            existingItem.setAvailable(itemDto.available());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Пользователь {} обновил вещь: {}", ownerId, updatedItem.getName());
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена."));

        List<CommentDto> comments = CommentMapper.toDto(commentRepository.findAllByItemId(itemId));

        if (item.getOwner().getId().equals(userId)) {
            return getItemResponseDtoWithOwnerBookings(item, comments);
        }

        log.info("Получена вещь с ID {} пользователем {}", itemId, userId);
        return ItemMapper.toDto(item, null, null, comments);
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(Long ownerId, int from, int size) {
        findUserById(ownerId);
        PageRequest page = PageRequest.of(from / size, size);

        List<Item> items = itemRepository.findAllByOwnerIdOrderById(ownerId, page);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Получаем все комментарии для всех вещей
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        Map<Long, List<CommentDto>> commentsMap = commentRepository.findAllByItemIdIn(itemIds).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.groupingBy(c -> items.stream()
                        .filter(i -> i.getId().equals(c.id()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("Вещь для комментария не найдена")).getId()));


        // 2. Получаем DTO с датами бронирования
        List<ItemResponseDto> dtos = items.stream()
                .map(item -> getItemResponseDtoWithOwnerBookings(
                        item, commentsMap.getOrDefault(item.getId(), Collections.emptyList())))
                .collect(Collectors.toList());

        log.info("Получен список вещей владельца {}. Количество: {}", ownerId, dtos.size());
        return dtos;
    }

    @Override
    public List<ItemDto> search(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        PageRequest page = PageRequest.of(from / size, size);

        List<Item> items = itemRepository.search(text, page);
        log.info("Выполнен поиск по тексту '{}'. Найдено: {}", text, items.size());
        return ItemMapper.toDto(items);
    }

    @Transactional
    @Override
    public CommentDto addComment(Long authorId, Long itemId, CommentCreateDto dto) {
        User author = findUserById(authorId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена."));

        // Проверка: автор должен быть арендатором и бронирование должно быть завершено
        LocalDateTime now = LocalDateTime.now();
        List<Booking> pastBookings = bookingRepository.findAllByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, authorId, BookingStatus.APPROVED, now);

        if (pastBookings.isEmpty()) {
            throw new ValidationException("Пользователь " + authorId + " не бронировал вещь " + itemId + " или бронирование не завершено.");
        }

        Comment comment = CommentMapper.toEntity(dto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);
        log.info("Пользователь {} добавил комментарий к вещи {}", authorId, itemId);

        return CommentMapper.toDto(savedComment);
    }

    /**
     * Приватный вспомогательный метод для получения DTO вещи с датами последнего/следующего бронирования
     * (только для владельца).
     */
    private ItemResponseDto getItemResponseDtoWithOwnerBookings(Item item, List<CommentDto> comments) {
        LocalDateTime now = LocalDateTime.now();

        // Last Booking
        BookingInItemDto lastBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(item.getId(), BookingStatus.APPROVED, now)
                .map(b -> new BookingInItemDto(b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd()))
                .orElse(null);

        // Next Booking
        BookingInItemDto nextBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                .map(b -> new BookingInItemDto(b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd()))
                .orElse(null);

        return ItemMapper.toDto(item, lastBooking, nextBooking, comments);
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