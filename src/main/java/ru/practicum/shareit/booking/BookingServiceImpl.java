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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Имплементация сервиса для работы с сущностью {@link Booking}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long bookerId, BookingCreateDto dto) {
        if (dto.start().isEqual(dto.end()) || dto.start().isAfter(dto.end())) {
            throw new ValidationException("Дата начала должна быть раньше даты окончания.");
        }

        User booker = findUserById(bookerId);
        Item item = itemRepository.findById(dto.itemId())
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + dto.itemId() + " не найдена."));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с ID " + dto.itemId() + " недоступна для бронирования.");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь.");
        }

        Booking booking = new Booking(null, dto.start(), dto.end(), item, booker, BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Создано бронирование ID {} арендатором {}", savedBooking.getId(), bookerId);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveOrReject(Long ownerId, Long bookingId, Boolean approved) {
        findUserById(ownerId); // Проверка существования владельца

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено."));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь " + ownerId + " не является владельцем вещи.");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Невозможно изменить статус уже подтвержденного/отклоненного бронирования.");
        }

        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Владелец {} установил статус {} для бронирования {}", ownerId, newStatus, bookingId);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        findUserById(userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено."));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Пользователь " + userId + " не является ни арендатором, ни владельцем.");
        }

        log.info("Получено бронирование ID {} пользователем {}", bookingId, userId);
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long bookerId, String state, int from, int size) {
        findUserById(bookerId);
        BookingState bookingState = parseState(state);
        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, page);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId, now, now, page);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, now, page);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, now, page);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.REJECTED, page);
                break;
            default:
                throw new ValidationException("Unknown state: " + state); // Должно быть обработано parseState
        }

        log.info("Получен список бронирований арендатора {} по состоянию {}. Количество: {}", bookerId, state, bookings.size());
        return BookingMapper.toDto(bookings);
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state, int from, int size) {
        findUserById(ownerId);
        BookingState bookingState = parseState(state);
        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        // Проверка: есть ли у пользователя вещи
        if (itemRepository.findAllByOwnerIdOrderById(ownerId, PageRequest.of(0, 1)).isEmpty()) {
            return Collections.emptyList();
        }

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findAllByOwnerId(ownerId, page);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentByOwnerId(ownerId, now, page);
                break;
            case PAST:
                bookings = bookingRepository.findAllPastByOwnerId(ownerId, now, page);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureByOwnerId(ownerId, now, page);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, page);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, page);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        log.info("Получен список бронирований для вещей владельца {} по состоянию {}. Количество: {}", ownerId, state, bookings.size());
        return BookingMapper.toDto(bookings);
    }

    /**
     * Парсит строковое состояние в Enum {@link BookingState}.
     * @param state Строка состояния.
     * @return Enum состояния.
     * @throws ValidationException Если состояние неизвестно.
     */
    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
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