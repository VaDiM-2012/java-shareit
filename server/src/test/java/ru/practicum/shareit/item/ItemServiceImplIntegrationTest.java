package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для сервиса вещей, проверяющие сохранение комментариев в базе данных.
 */
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item saveItem(User owner, String name) {
        return itemRepository.save(new Item(null, name, "Описание " + name, true, owner, null));
    }

    /**
     * Тест проверяет успешное добавление комментария к вещи и его сохранение в БД,
     * а также проверку наличия завершенного бронирования.
     */
    @Test
    void addComment_shouldSaveCommentInDb() {
        // ARRANGE: Создание владельца, автора, вещи и завершенного бронирования.
        User owner = saveUser("Owner", "owner4@mail.com");
        User author = saveUser("Author", "author4@mail.com");
        Item item = saveItem(owner, "Стол");

        // Создаем завершенное и одобренное бронирование
        LocalDateTime pastStart = LocalDateTime.now().minusDays(5);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(3);
        bookingRepository.save(new Booking(null, pastStart, pastEnd, item, author, BookingStatus.APPROVED));

        CommentCreateDto commentDto = new CommentCreateDto("Очень полезный стол!");

        // ACT: Вызов тестируемого метода
        CommentDto savedCommentDto = itemService.addComment(author.getId(), item.getId(), commentDto);

        // ASSERT: Проверка, что комментарий сохранен в БД и имеет корректные поля.
        List<Comment> commentsInDb = commentRepository.findAllByItemId(item.getId());

        assertFalse(commentsInDb.isEmpty(), "Комментарий должен быть сохранен в БД.");
        assertEquals(1, commentsInDb.size(), "Должен быть сохранен ровно один комментарий.");
        assertEquals(commentDto.text(), commentsInDb.getFirst().getText(), "Текст комментария должен совпадать.");
        assertEquals(author.getId(), commentsInDb.getFirst().getAuthor().getId(), "ID автора комментария должен совпадать.");
        assertEquals(item.getId(), commentsInDb.getFirst().getItem().getId(), "ID вещи должен совпадать.");
    }

    /**
     * Тест проверяет, что комментарий нельзя добавить без завершенного бронирования.
     */
    @Test
    void addComment_shouldThrowExceptionIfNoPastBooking() {
        // ARRANGE: Создание владельца, автора, вещи. Бронирование отсутствует.
        User owner = saveUser("Owner5", "owner5@mail.com");
        User author = saveUser("Author5", "author5@mail.com");
        Item item = saveItem(owner, "Стул");

        CommentCreateDto commentDto = new CommentCreateDto("Хочу написать комментарий, но не бронировал!");

        // ACT & ASSERT: Попытка добавить комментарий должна вызвать исключение.
        assertThrows(BookingNotFoundException.class, () -> itemService.addComment(author.getId(), item.getId(), commentDto), "Добавление комментария без завершенного бронирования должно вызвать BookingNotFoundException.");
    }
}