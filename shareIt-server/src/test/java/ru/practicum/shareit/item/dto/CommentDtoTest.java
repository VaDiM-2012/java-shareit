package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректности JSON-сериализации и десериализации
 * DTO CommentDto, содержащего поле LocalDateTime (created).
 */
@JsonTest
@ActiveProfiles("test")
public class CommentDtoTest {

    /**
     * Джексон-тестер для {@link CommentDto}.
     */
    @Autowired
    private JacksonTester<CommentDto> jacksonTester;

    /**
     * Тестирование корректности сериализации объекта {@link CommentDto} в JSON-строку.
     * Проверяет, что поле created (LocalDateTime) сериализуется в ожидаемом формате.
     *
     * @throws IOException если произошла ошибка при сериализации.
     */
    @Test
    void testSerialize() throws IOException {
        // 1. Подготовка: Создание DTO с фиксированными значениями
        LocalDateTime createdDateTime = LocalDateTime.of(2024, 5, 15, 8, 45, 10);
        CommentDto dto = new CommentDto(
                1L,
                "Это тестовый комментарий.",
                "Тестовый Автор",
                createdDateTime
        );

        // Ожидаемое строковое представление даты в JSON
        String expectedCreatedJson = "2024-05-15T08:45:10";

        // 2. Действие: Сериализация DTO
        JsonContent<CommentDto> result = this.jacksonTester.write(dto);

        // 3. Проверка: Проверяем поля JSON
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Это тестовый комментарий.");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Тестовый Автор");

        // Проверка формата LocalDateTime
        assertThat(result)
                .extractingJsonPathStringValue("$.created")
                .as("Проверка формата сериализации даты создания (created)")
                .isEqualTo(expectedCreatedJson);
    }

    /**
     * Тестирование корректности десериализации объекта {@link CommentDto} из JSON-строки.
     * Проверяет, что LocalDateTime корректно восстанавливается.
     *
     * @throws IOException если произошла ошибка при десериализации.
     */
    @Test
    void testDeserialize() throws IOException {
        // 1. Подготовка: JSON-строка и ожидаемый DTO
        String jsonContent = "{\"id\": 2, \"text\": \"Отличная вещь!\", \"authorName\": \"Пользователь 3\", \"created\": \"2024-05-15T08:45:10\"}";

        LocalDateTime expectedCreated = LocalDateTime.of(2024, 5, 15, 8, 45, 10);

        CommentDto expectedDto = new CommentDto(
                2L,
                "Отличная вещь!",
                "Пользователь 3",
                expectedCreated
        );

        // 2. Действие: Десериализация JSON
        CommentDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // 3. Проверка: Сравнение полученного объекта с ожидаемым
        assertThat(resultDto)
                .as("Проверка полного соответствия десериализованного объекта")
                .isEqualTo(expectedDto);

        // Дополнительная проверка поля created
        assertThat(resultDto.created())
                .as("Проверка поля created (LocalDateTime) после десериализации")
                .isEqualTo(expectedCreated);
    }
}