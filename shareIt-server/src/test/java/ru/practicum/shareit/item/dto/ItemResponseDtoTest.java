package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.comment.dto.CommentDto; // Импорт для вложенного списка

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректности JSON-сериализации и десериализации
 * DTO ItemResponseDto. Проверяет вложенные объекты с датами (BookingInItemDto)
 * и список вложенных объектов (List<CommentDto>).
 */
@JsonTest
@ActiveProfiles("test")
public class ItemResponseDtoTest {

    /**
     * Джексон-тестер для {@link ItemResponseDto}.
     */
    @Autowired
    private JacksonTester<ItemResponseDto> jacksonTester;

    /**
     * Тестирование корректности сериализации объекта {@link ItemResponseDto} в JSON-строку.
     * Проверяет формат LocalDateTime во вложенных бронированиях и структуру комментариев.
     *
     * @throws IOException
     */
    @Test
    void testSerialize() throws IOException {
        // 1. Подготовка: Создание DTO с вложенными объектами
        Long itemId = 1L;
        Long requestId = 10L;

        // Вложенные DTO
        LocalDateTime lastBookingStart = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        LocalDateTime nextBookingStart = LocalDateTime.of(2025, 2, 1, 15, 0, 0);

        BookingInItemDto lastBooking = new BookingInItemDto(100L, 5L, lastBookingStart, lastBookingStart.plusDays(1));
        BookingInItemDto nextBooking = new BookingInItemDto(101L, 6L, nextBookingStart, nextBookingStart.plusDays(1));

        // Список комментариев
        CommentDto comment1 = new CommentDto(1L, "Отлично!", "Пользователь А", LocalDateTime.of(2024, 10, 1, 9, 0, 0));
        List<CommentDto> comments = List.of(comment1);

        ItemResponseDto dto = new ItemResponseDto(itemId, "Дрель", "Мощная дрель", true, requestId, lastBooking, nextBooking, comments);

        // Ожидаемые строковые представления дат
        String expectedLastStartJson = "2025-01-01T10:00:00";
        String expectedNextStartJson = "2025-02-01T15:00:00";
        String expectedCommentCreatedJson = "2024-10-01T09:00:00";

        // 2. Действие: Сериализация DTO
        JsonContent<ItemResponseDto> result = this.jacksonTester.write(dto);

        // 3. Проверка: Проверяем поля JSON
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();

        // Проверка вложенного объекта lastBooking
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.lastBooking.start").as("Проверка формата даты в lastBooking").isEqualTo(expectedLastStartJson);

        // Проверка вложенного объекта nextBooking
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(101);
        assertThat(result).extractingJsonPathStringValue("$.nextBooking.start").as("Проверка формата даты в nextBooking").isEqualTo(expectedNextStartJson);

        // Проверка списка комментариев
        assertThat(result).extractingJsonPathValue("$.comments").asList().hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Отлично!");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created").as("Проверка формата даты в CommentDto").isEqualTo(expectedCommentCreatedJson);
    }

    /**
     * Тестирование корректности десериализации объекта {@link ItemResponseDto} из JSON-строки.
     * Проверяет корректное восстановление всех вложенных DTO и их полей LocalDateTime.
     *
     * @throws IOException
     */
    @Test
    void testDeserialize() throws IOException {
        // 1. Подготовка: JSON-строка со сложной структурой
        String jsonContent = """
            {
                "id": 2,
                "name": "Телескоп",
                "description": "Мощный телескоп",
                "available": true,
                "requestId": 20,
                "lastBooking": {
                    "id": 200,
                    "bookerId": 7,
                    "start": "2025-03-01T10:00:00",
                    "end": "2025-03-02T10:00:00"
                },
                "nextBooking": null,
                "comments": [
                    {
                        "id": 5,
                        "text": "Очень доволен!",
                        "authorName": "Юзер 1",
                        "created": "2024-11-05T12:00:00"
                    }
                ]
            }""";

        // Ожидаемые DTO-объекты
        LocalDateTime expectedLastStart = LocalDateTime.of(2025, 3, 1, 10, 0, 0);
        LocalDateTime expectedLastEnd = LocalDateTime.of(2025, 3, 2, 10, 0, 0);
        BookingInItemDto expectedLastBooking = new BookingInItemDto(200L, 7L, expectedLastStart, expectedLastEnd);

        LocalDateTime expectedCommentCreated = LocalDateTime.of(2024, 11, 5, 12, 0, 0);
        CommentDto expectedComment = new CommentDto(5L, "Очень доволен!", "Юзер 1", expectedCommentCreated);

        ItemResponseDto expectedDto = new ItemResponseDto(
                2L, "Телескоп", "Мощный телескоп", true, 20L,
                expectedLastBooking, null, List.of(expectedComment)
        );

        // 2. Действие: Десериализация JSON
        ItemResponseDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // 3. Проверка: Сравнение полученного объекта с ожидаемым
        assertThat(resultDto).isEqualTo(expectedDto);

        // Дополнительная детальная проверка вложенных данных
        assertThat(resultDto.lastBooking().start())
                .as("Проверка даты начала в lastBooking после десериализации")
                .isEqualTo(expectedLastStart);
        assertThat(resultDto.nextBooking())
                .as("Проверка, что nextBooking десериализован как null")
                .isNull();
        assertThat(resultDto.comments().getFirst().created())
                .as("Проверка даты создания комментария после десериализации")
                .isEqualTo(expectedCommentCreated);
    }
}