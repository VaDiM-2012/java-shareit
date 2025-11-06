package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.repository.CommentRepository;

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

    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository, null);
        owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@example.com"));
        item = itemRepository.save(new Item(null, "Drill", "Power drill", true, owner, null));
    }

    @Test
    void createItem_success() {
        ItemDto itemDto = new ItemDto(null, "Hammer", "Heavy hammer", true, null);
        ItemDto createdItem = itemService.create(owner.getId(), itemDto);

        assertNotNull(createdItem.id());
        assertEquals("Hammer", createdItem.name());
        assertEquals("Heavy hammer", createdItem.description());
        assertTrue(createdItem.available());
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
        Booking booking = new Booking(null, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1),
                item, booker, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        ItemResponseDto responseDto = itemService.getById(owner.getId(), item.getId());
        assertNotNull(responseDto.lastBooking());
        assertEquals(booking.getId(), responseDto.lastBooking().id());
        assertEquals(booker.getId(), responseDto.lastBooking().bookerId());
    }

    @Test
    void searchItems_success() {
        itemRepository.save(new Item(null, "Screwdriver", "Flathead screwdriver", true, owner, null));
        List<ItemDto> items = itemService.search("screw", 0, 10);
        assertEquals(1, items.size());
        assertEquals("Screwdriver", items.getFirst().name());
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
}