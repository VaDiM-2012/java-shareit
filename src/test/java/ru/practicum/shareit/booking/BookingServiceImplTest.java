package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
}