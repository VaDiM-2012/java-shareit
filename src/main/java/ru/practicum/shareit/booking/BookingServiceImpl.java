package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Реализация сервиса бронирований.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    /**
     * Создает бронирование.
     * @param bookerId ID арендатора.
     * @param dto Данные.
     * @return Созданное бронирование.
     * @throws ItemNotAvailableException если даты некорректны или вещь недоступна.
     * @throws NotFoundException если вещь или пользователь не найдены.
     */
    @Transactional
    @Override
    public BookingResponseDto create(Long bookerId, BookingCreateDto dto) {
        if (dto.start().isEqual(dto.end()) || dto.start().isAfter(dto.end())) {
            throw new ItemNotAvailableException("Дата начала должна быть раньше окончания.");
        }

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь ID " + bookerId + " не найден."));
        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new NotFoundException("Вещь ID " + dto.itemId() + " не найдена."));

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь ID " + dto.itemId() + " недоступна.");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь.");
        }

        Booking booking = new Booking(null, dto.start(), dto.end(), item, booker, BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Создано бронирование ID {} арендатором {}", savedBooking.getId(), bookerId);
        return BookingMapper.toDto(savedBooking);
    }

    /**
     * Одобряет или отклоняет бронирование вещи.
     * Только владелец вещи может изменить статус бронирования, и бронирование должно находиться в статусе WAITING.
     *
     * @param ownerId   Идентификатор пользователя, который является владельцем вещи.
     * @param bookingId Идентификатор бронирования.
     * @param approved  Флаг одобрения: true для одобрения (APPROVED), false для отклонения (REJECTED).
     * @return DTO обновленного бронирования {@link BookingResponseDto}.
     * @throws NotFoundException      если бронирование не найдено.
     * @throws OwnerMismatchException если пользователь не является владельцем вещи.
     * @throws ValidationException    если статус бронирования уже изменен (не WAITING).
     */
    @Transactional
    @Override
    public BookingResponseDto approveOrReject(Long ownerId, Long bookingId, Boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование ID " + bookingId + " не найдено."));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new OwnerMismatchException("Пользователь ID " + ownerId + " не является владельцем вещи.");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Невозможно изменить статус уже обработанного бронирования.");
        }

        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Владелец {} установил статус {} для бронирования {}", ownerId, newStatus, bookingId);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        checkUserExists(userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование ID " + bookingId + " не найдено."));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Пользователь " + userId + " не арендатор и не владелец.");
        }

        log.info("Получено бронирование ID {} пользователем {}", bookingId, userId);
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long bookerId, String state, int from, int size) {
        checkUserExists(bookerId);
        BookingState bookingState = parseState(state);
        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime currentTime = LocalDateTime.now();
        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, page);
            case CURRENT -> bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, currentTime, currentTime, page);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, currentTime, page);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, currentTime, page);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING, page);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED, page);
        };

        log.info("Получен список бронирований арендатора {} по состоянию {}. Количество: {}", bookerId, state, bookings.size());
        return BookingMapper.toDto(bookings);
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state, int from, int size) {
        checkUserExists(ownerId);
        BookingState bookingState = parseState(state);
        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime currentTime = LocalDateTime.now();
        List<Booking> bookings;

        if (itemRepository.findAllByOwnerIdOrderById(ownerId, PageRequest.of(0, 1)).isEmpty()) {
            return Collections.emptyList();
        }

        bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByOwnerId(ownerId, page);
            case CURRENT -> bookingRepository.findAllCurrentByOwnerId(ownerId, currentTime, page);
            case PAST -> bookingRepository.findAllPastByOwnerId(ownerId, currentTime, page);
            case FUTURE -> bookingRepository.findAllFutureByOwnerId(ownerId, currentTime, page);
            case WAITING -> bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, page);
            case REJECTED -> bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, page);
        };

        log.info("Получен список бронирований для вещей владельца {} по состоянию {}. Количество: {}", ownerId, state, bookings.size());
        return BookingMapper.toDto(bookings);
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }

    private void checkUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь ID " + userId + " не найден."));
    }
}