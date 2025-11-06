package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerMismatchException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class BookingServiceImplTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
        owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@example.com"));
        item = itemRepository.save(new Item(null, "Drill", "Power drill", true, owner, null));
    }

    @Test
    void createBooking_success() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingCreateDto bookingDto = new BookingCreateDto(item.getId(), start, end);

        BookingResponseDto result = bookingService.create(booker.getId(), bookingDto);

        assertNotNull(result.id());
        assertEquals(start, result.start());
        assertEquals(end, result.end());
        assertEquals(BookingStatus.WAITING, result.status());
        assertEquals(booker.getId(), result.booker().id());
        assertEquals(item.getId(), result.item().id());
    }

    @Test
    void createBooking_itemNotAvailable_throwsException() {
        item.setAvailable(false);
        itemRepository.save(item);
        BookingCreateDto bookingDto = new BookingCreateDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        assertThrows(ItemNotAvailableException.class, () -> bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void createBooking_ownerBooksOwnItem_throwsException() {
        BookingCreateDto bookingDto = new BookingCreateDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(), bookingDto));
    }

    @Test
    void approveBooking_success() {
        Booking booking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);

        BookingResponseDto result = bookingService.approveOrReject(owner.getId(), savedBooking.getId(), true);

        assertEquals(BookingStatus.APPROVED, result.status());
    }

    @Test
    void approveBooking_notOwner_throwsException() {
        Booking booking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING);
        Booking savedBooking = bookingRepository.save(booking);

        assertThrows(OwnerMismatchException.class, () -> bookingService.approveOrReject(booker.getId(), savedBooking.getId(), true));
    }

    @Test
    void approveBooking_alreadyApproved_throwsException() {
        Booking booking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED);
        Booking savedBooking = bookingRepository.save(booking);

        assertThrows(ValidationException.class, () -> bookingService.approveOrReject(owner.getId(), savedBooking.getId(), true));
    }

    @Test
    void getAllByBooker_currentState() {
        Booking booking = new Booking(null, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<BookingResponseDto> bookings = bookingService.getAllByBooker(booker.getId(), "CURRENT", 0, 10);
        assertEquals(1, bookings.size());
        assertEquals(booking.getId(), bookings.getFirst().id());
    }

    @Test
    void getById_success_asBooker() {
        Booking booking = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        BookingResponseDto result = bookingService.getById(booker.getId(), booking.getId());

        assertEquals(booking.getId(), result.id());
        assertEquals(booker.getId(), result.booker().id());
    }

    @Test
    void getById_success_asOwner() {
        Booking booking = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        BookingResponseDto result = bookingService.getById(owner.getId(), booking.getId());

        assertEquals(booking.getId(), result.id());
        assertEquals(item.getId(), result.item().id());
    }

    @Test
    void getById_notBookerNorOwner_throwsException() {
        User stranger = userRepository.save(new User(null, "Stranger", "stranger@example.com"));
        Booking booking = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        assertThrows(NotFoundException.class, () -> bookingService.getById(stranger.getId(), booking.getId()));
    }

    @Test
    void getById_bookingNotFound_throwsException() {
        assertThrows(NotFoundException.class, () -> bookingService.getById(booker.getId(), 999L));
    }

    @Test
    void getAllByBooker_allState() {
        Booking b1 = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED));
        Booking b2 = bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), item, booker, BookingStatus.REJECTED));

        List<BookingResponseDto> result = bookingService.getAllByBooker(booker.getId(), "ALL", 0, 10);

        assertEquals(2, result.size());
        assertEquals(b1.getId(), result.get(0).id());
        assertEquals(b2.getId(), result.get(1).id());
    }

    @Test
    void getAllByBooker_pastState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED));
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        List<BookingResponseDto> result = bookingService.getAllByBooker(booker.getId(), "PAST", 0, 10);

        assertEquals(1, result.size());
        assertTrue(result.get(0).end().isBefore(LocalDateTime.now()));
    }

    @Test
    void getAllByBooker_futureState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED));
        Booking future = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        List<BookingResponseDto> result = bookingService.getAllByBooker(booker.getId(), "FUTURE", 0, 10);

        assertEquals(1, result.size());
        assertEquals(future.getId(), result.get(0).id());
    }

    @Test
    void getAllByBooker_waitingState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED));
        Booking waiting = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4), item, booker, BookingStatus.WAITING));

        List<BookingResponseDto> result = bookingService.getAllByBooker(booker.getId(), "WAITING", 0, 10);

        assertEquals(1, result.size());
        assertEquals(waiting.getId(), result.get(0).id());
    }

    @Test
    void getAllByBooker_rejectedState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED));
        Booking rejected = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4), item, booker, BookingStatus.REJECTED));

        List<BookingResponseDto> result = bookingService.getAllByBooker(booker.getId(), "REJECTED", 0, 10);

        assertEquals(1, result.size());
        assertEquals(rejected.getId(), result.get(0).id());
    }

    @Test
    void getAllByBooker_invalidState_throwsException() {
        assertThrows(ValidationException.class, () ->
                bookingService.getAllByBooker(booker.getId(), "INVALID", 0, 10));
    }

    @Test
    void getAllByBooker_pagination() {
        for (int i = 0; i < 5; i++) {
            bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(i + 1),
                    LocalDateTime.now().plusDays(i + 2), item, booker, BookingStatus.APPROVED));
        }

        List<BookingResponseDto> page1 = bookingService.getAllByBooker(booker.getId(), "ALL", 0, 2);
        List<BookingResponseDto> page2 = bookingService.getAllByBooker(booker.getId(), "ALL", 2, 2);

        assertEquals(2, page1.size());
        assertEquals(2, page2.size());
        assertNotEquals(page1.get(0).id(), page2.get(0).id());
    }

    @Test
    void getAllByOwner_allState() {
        Item item2 = itemRepository.save(new Item(null, "Saw", "Hand saw", true, owner, null));
        Booking b1 = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED));
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(1), item2, booker, BookingStatus.APPROVED));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), "ALL", 0, 10);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(b -> b.id().equals(b1.getId())));
    }

    @Test
    void getAllByOwner_currentState() {
        Item item2 = itemRepository.save(new Item(null, "Saw", "Hand saw", true, owner, null));
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED));
        Booking current = bookingRepository.save(new Booking(null, LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1), item2, booker, BookingStatus.APPROVED));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), "CURRENT", 0, 10);

        assertEquals(1, result.size());
        assertEquals(current.getId(), result.get(0).id());
    }

    @Test
    void getAllByOwner_pastState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED));
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), "PAST", 0, 10);

        assertEquals(1, result.size());
        assertTrue(result.get(0).end().isBefore(LocalDateTime.now()));
    }

    @Test
    void getAllByOwner_futureState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED));
        Booking future = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), "FUTURE", 0, 10);

        assertEquals(1, result.size());
        assertEquals(future.getId(), result.get(0).id());
    }

    @Test
    void getAllByOwner_waitingState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED));
        Booking waiting = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4), item, booker, BookingStatus.WAITING));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), "WAITING", 0, 10);

        assertEquals(1, result.size());
        assertEquals(waiting.getId(), result.get(0).id());
    }

    @Test
    void getAllByOwner_rejectedState() {
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED));
        Booking rejected = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4), item, booker, BookingStatus.REJECTED));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), "REJECTED", 0, 10);

        assertEquals(1, result.size());
        assertEquals(rejected.getId(), result.get(0).id());
    }

    @Test
    void getAllByOwner_noItems_returnsEmptyList() {
        User anotherOwner = userRepository.save(new User(null, "Another", "another@example.com"));

        List<BookingResponseDto> result = bookingService.getAllByOwner(anotherOwner.getId(), "ALL", 0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllByOwner_invalidState_throwsException() {
        assertThrows(ValidationException.class, () ->
                bookingService.getAllByOwner(owner.getId(), "INVALID", 0, 10));
    }

    @Test
    void approveBooking_reject_success() {
        Booking booking = bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.WAITING));

        BookingResponseDto result = bookingService.approveOrReject(owner.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, result.status());
    }

    @Test
    void createBooking_invalidDates_startEqualsEnd_throwsException() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        BookingCreateDto dto = new BookingCreateDto(item.getId(), time, time);

        assertThrows(ItemNotAvailableException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBooking_invalidDates_startAfterEnd_throwsException() {
        BookingCreateDto dto = new BookingCreateDto(item.getId(),
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        assertThrows(ItemNotAvailableException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBooking_itemNotFound_throwsException() {
        BookingCreateDto dto = new BookingCreateDto(999L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void createBooking_bookerNotFound_throwsException() {
        BookingCreateDto dto = new BookingCreateDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(999L, dto));
    }
}