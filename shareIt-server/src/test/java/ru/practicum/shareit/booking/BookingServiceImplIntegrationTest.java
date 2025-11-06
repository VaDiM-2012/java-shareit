package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для сервиса бронирования, проверяющие взаимодействие с базой данных.
 * Используется профиль "test" для работы с тестовой базой данных.
 */
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item saveItem(User owner, String name) {
        return itemRepository.save(new Item(null, name, "Описание " + name, true, owner, null));
    }

    private Booking saveBooking(User booker, Item item, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return bookingRepository.save(new Booking(null, start, end, item, booker, status));
    }

    /**
     * Тест проверяет успешное создание и сохранение бронирования в базе данных.
     * Проверяет, что статус WAITING присвоен корректно.
     */
    @Test
    void create_shouldSaveBookingWithWaitingStatus() {
        // ARRANGE: Создание и сохранение владельца, арендатора и доступной вещи.
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Дрель");
        
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        
        BookingCreateDto bookingDto = new BookingCreateDto(item.getId(), start, end);

        // ACT: Вызов тестируемого метода сервиса
        BookingResponseDto createdBookingDto = bookingService.create(booker.getId(), bookingDto);

        // ASSERT: Проверка результата вызова и состояния базы данных.
        Optional<Booking> savedBooking = bookingRepository.findById(createdBookingDto.id());

        assertTrue(savedBooking.isPresent(), "Бронирование должно быть сохранено в БД.");
        assertEquals(BookingStatus.WAITING, savedBooking.get().getStatus(), "Статус должен быть WAITING.");
        assertEquals(booker.getId(), savedBooking.get().getBooker().getId(), "ID арендатора должен совпадать.");
        assertEquals(item.getId(), savedBooking.get().getItem().getId(), "ID вещи должен совпадать.");
    }

    /**
     * Тест проверяет успешное одобрение бронирования владельцем и корректное обновление статуса в БД.
     */
    @Test
    void approveOrReject_shouldApproveAndChangeStatusInDb() {
        // ARRANGE: Создание владельца, арендатора, вещи и бронирования со статусом WAITING.
        User owner = saveUser("Owner", "owner2@mail.com");
        User booker = saveUser("Booker", "booker2@mail.com");
        Item item = saveItem(owner, "Молоток");
        
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        
        Booking waitingBooking = saveBooking(booker, item, start, end, BookingStatus.WAITING);

        // ACT: Владелец одобряет бронирование
        BookingResponseDto approvedBookingDto = bookingService.approveOrReject(owner.getId(), waitingBooking.getId(), true);

        // ASSERT: Проверка, что в БД статус бронирования изменился на APPROVED.
        Optional<Booking> savedBooking = bookingRepository.findById(approvedBookingDto.id());

        assertTrue(savedBooking.isPresent(), "Бронирование должно существовать в БД.");
        assertEquals(BookingStatus.APPROVED, savedBooking.get().getStatus(), "Статус должен быть APPROVED после одобрения.");
        assertEquals(approvedBookingDto.status(), savedBooking.get().getStatus(), "Статус в DTO должен совпадать.");
    }
    
    /**
     * Тест проверяет фильтрацию бронирований по состоянию "PAST" для арендатора.
     */
    @Test
    void getAllByBooker_shouldReturnPastBookings() {
        // ARRANGE: Создание владельца, арендатора и двух бронирований: PAST и FUTURE.
        User owner = saveUser("Owner", "owner3@mail.com");
        User booker = saveUser("Booker", "booker3@mail.com");
        Item item = saveItem(owner, "Отвертка");

        // Бронирование в прошлом (PAST)
        LocalDateTime pastStart = LocalDateTime.now().minusDays(5);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(3);
        saveBooking(booker, item, pastStart, pastEnd, BookingStatus.APPROVED);

        // Бронирование в будущем (FUTURE)
        LocalDateTime futureStart = LocalDateTime.now().plusDays(3);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(5);
        saveBooking(booker, item, futureStart, futureEnd, BookingStatus.WAITING);

        // ACT: Получение бронирований арендатора с фильтром "PAST".
        List<BookingResponseDto> pastBookings = bookingService.getAllByBooker(booker.getId(), "PAST", 0, 10);

        // ASSERT: Проверка, что в списке только одно бронирование (PAST), и его даты в прошлом.
        assertEquals(1, pastBookings.size(), "Должно быть возвращено только одно прошлое бронирование.");
        assertTrue(pastBookings.getFirst().end().isBefore(LocalDateTime.now().plusSeconds(1)), "Дата окончания должна быть в прошлом.");
    }
}