package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class ItemServiceImplTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRequestRepository requestRepository; // Добавляем ItemRequestRepository

    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private User otherUser; // Для тестов, где нужен не-владелец
    private Item item;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        // Передаем ItemRequestRepository в конструктор
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository, requestRepository);

        owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@example.com"));
        otherUser = userRepository.save(new User(null, "Other", "other@example.com"));
        item = itemRepository.save(new Item(null, "Drill", "Power drill", true, owner, null));

        // Сохраняем ItemRequest для теста
        itemRequest = requestRepository.save(new ItemRequest(null, "Request for a hammer", booker, LocalDateTime.now()));
    }

    // --- Существующие тесты (для полноты кода) ---

    @Test
    void createItem_success() {
        ItemDto itemDto = new ItemDto(null, "Hammer", "Heavy hammer", true, null);
        ItemDto createdItem = itemService.create(owner.getId(), itemDto);

        assertNotNull(createdItem.id());
        assertEquals("Hammer", createdItem.name());
        assertEquals("Heavy hammer", createdItem.description());
        assertTrue(createdItem.available());
    }

    // --- ДОБАВЛЕННЫЕ ТЕСТЫ ---

    @Test
    void createItem_withRequest_success() {
        ItemDto itemDto = new ItemDto(null, "Screwdriver", "Small screwdriver", true, itemRequest.getId());
        ItemDto createdItem = itemService.create(owner.getId(), itemDto);

        assertNotNull(createdItem.id());
        assertEquals(itemRequest.getId(), createdItem.requestId()); // Проверяем, что requestId сохранен
        assertEquals("Screwdriver", createdItem.name());
    }

    @Test
    void createItem_withNonExistingRequest_throwsException() {
        Long nonExistingRequestId = 999L;
        ItemDto itemDto = new ItemDto(null, "Wrench", "Adjustable wrench", true, nonExistingRequestId);

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), itemDto),
                "Должно выброситься исключение, если запрос не существует.");
    }

    @Test
    void updateItem_partialUpdate_success() {
        // Обновляем только доступность
        ItemDto updateDto = new ItemDto(null, null, null, false, null);
        ItemDto updatedItem = itemService.update(owner.getId(), item.getId(), updateDto);

        assertEquals(item.getId(), updatedItem.id());
        assertEquals("Drill", updatedItem.name()); // Имя не должно измениться
        assertEquals("Power drill", updatedItem.description()); // Описание не должно измениться
        assertFalse(updatedItem.available()); // Доступность должна измениться
    }

    @Test
    void updateItem_itemNotFound_throwsException() {
        ItemDto updateDto = new ItemDto(null, "Nonexistent", null, true, null);
        Long nonExistingItemId = 999L;
        assertThrows(NotFoundException.class, () -> itemService.update(owner.getId(), nonExistingItemId, updateDto),
                "Должно выброситься исключение, если вещь не найдена.");
    }

    @Test
    void updateItem_success() {
        ItemDto updateDto = new ItemDto(null, "Updated Drill", "Updated description", false, null);
        ItemDto updatedItem = itemService.update(owner.getId(), item.getId(), updateDto);

        assertEquals(item.getId(), updatedItem.id());
        assertEquals("Updated Drill", updatedItem.name());
        assertEquals("Updated description", updatedItem.description());
        assertFalse(updatedItem.available());
    }

    @Test
    void updateItem_notOwner_throwsException() {
        ItemDto updateDto = new ItemDto(null, "Updated Drill", "Updated description", false, null);
        assertThrows(NotFoundException.class, () -> itemService.update(booker.getId(), item.getId(), updateDto));
    }

    @Test
    void getById_owner_includesBookings() {
        LocalDateTime now = LocalDateTime.now();
        // Прошлое бронирование (Last Booking)
        Booking pastBooking = new Booking(null, now.minusDays(3), now.minusDays(2),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        // Будущее бронирование (Next Booking)
        Booking futureBooking = new Booking(null, now.plusDays(1), now.plusDays(2),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        ItemResponseDto responseDto = itemService.getById(owner.getId(), item.getId());

        assertNotNull(responseDto.lastBooking());
        assertEquals(pastBooking.getId(), responseDto.lastBooking().id());
        assertEquals(booker.getId(), responseDto.lastBooking().bookerId());

        assertNotNull(responseDto.nextBooking());
        assertEquals(futureBooking.getId(), responseDto.nextBooking().id());
        assertEquals(booker.getId(), responseDto.nextBooking().bookerId());
    }

    @Test
    void getById_notOwner_noBookings() {
        Booking booking = new Booking(null, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        ItemResponseDto responseDto = itemService.getById(otherUser.getId(), item.getId());

        assertNull(responseDto.lastBooking(), "Не-владелец не должен видеть lastBooking.");
        assertNull(responseDto.nextBooking(), "Не-владелец не должен видеть nextBooking.");
    }

    @Test
    void getById_itemNotFound_throwsException() {
        Long nonExistingItemId = 999L;
        assertThrows(NotFoundException.class, () -> itemService.getById(owner.getId(), nonExistingItemId),
                "Должно выброситься исключение, если вещь не найдена.");
    }

    @Test
    void getAllByOwner_success_withBookings() {
        // Создадим вторую вещь для владельца
        Item item2 = itemRepository.save(new Item(null, "Plane", "Wood plane", true, owner, null));

        LocalDateTime now = LocalDateTime.now();
        // Бронирование для item (Last Booking)
        Booking pastBooking = new Booking(null, now.minusDays(3), now.minusDays(2), item, booker, BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        // Бронирование для item2 (Next Booking)
        Booking futureBooking = new Booking(null, now.plusDays(1), now.plusDays(2), item2, booker, BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        List<ItemResponseDto> items = itemService.getAllByOwner(owner.getId(), 0, 10);
        assertEquals(2, items.size());

        // Проверка item
        ItemResponseDto itemDto = items.stream().filter(i -> i.id().equals(item.getId())).findFirst().orElseThrow();
        assertNotNull(itemDto.lastBooking());
        assertNull(itemDto.nextBooking());

        // Проверка item2
        ItemResponseDto item2Dto = items.stream().filter(i -> i.id().equals(item2.getId())).findFirst().orElseThrow();
        assertNull(item2Dto.lastBooking());
        assertNotNull(item2Dto.nextBooking());
    }

    @Test
    void getAllByOwner_emptyList() {
        User lonelyUser = userRepository.save(new User(null, "Lonely", "lonely@example.com"));
        List<ItemResponseDto> items = itemService.getAllByOwner(lonelyUser.getId(), 0, 10);
        assertTrue(items.isEmpty(), "Должен быть возвращен пустой список, если у пользователя нет вещей.");
    }

    @Test
    void searchItems_success() {
        itemRepository.save(new Item(null, "Screwdriver", "Flathead screwdriver", true, owner, null));
        List<ItemDto> items = itemService.search("screw", 0, 10);
        assertEquals(1, items.size());
        assertEquals("Screwdriver", items.getFirst().name());
    }

    @Test
    void searchItems_textNotAvailable_emptyList() {
        // Вещь недоступна
        itemRepository.save(new Item(null, "Unavailable Saw", "A broken saw", false, owner, null));

        // Поиск должен найти только доступные
        List<ItemDto> items = itemService.search("saw", 0, 10);
        assertTrue(items.isEmpty(), "Поиск должен возвращать только доступные (available=true) вещи.");
    }

    @Test
    void addComment_success() {
        Booking booking = new Booking(null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentCreateDto commentDto = new CommentCreateDto("Great item!");
        CommentDto result = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertNotNull(result.id());
        assertEquals("Great item!", result.text());
        assertEquals(booker.getName(), result.authorName());
    }

    @Test
    void addComment_noBooking_throwsException() {
        CommentCreateDto commentDto = new CommentCreateDto("Great item!");
        assertThrows(BookingNotFoundException.class, () -> itemService.addComment(booker.getId(), item.getId(), commentDto));
    }

    @Test
    void addComment_noPastBooking_throwsException() {
        LocalDateTime now = LocalDateTime.now();
        // Бронирование, которое еще не завершилось (текущее или будущее)
        Booking currentBooking = new Booking(null, now.minusHours(1), now.plusHours(1),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(currentBooking);

        CommentCreateDto commentDto = new CommentCreateDto("Trying to comment early!");

        assertThrows(BookingNotFoundException.class, () -> itemService.addComment(booker.getId(), item.getId(), commentDto),
                "Комментарий должен быть разрешен только после завершения бронирования.");
    }

    @Test
    void addComment_itemNotFound_throwsException() {
        Long nonExistingItemId = 999L;
        CommentCreateDto commentDto = new CommentCreateDto("Test comment");
        assertThrows(NotFoundException.class, () -> itemService.addComment(booker.getId(), nonExistingItemId, commentDto),
                "Должно выброситься исключение, если вещь не найдена.");
    }
}