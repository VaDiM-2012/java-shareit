package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.model.BookingStatus; // Импорт необходим

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректности JSON-сериализации и десериализации
 * DTO BookingDto, включая поля LocalDateTime и Enum.
 */
@JsonTest
@ActiveProfiles("test")
public class BookingDtoTest {

    /**
     * Джексон-тестер для {@link BookingDto}.
     */
    @Autowired
    private JacksonTester<BookingDto> jacksonTester;

    /**
     * Тестирование корректности сериализации объекта {@link BookingDto} в JSON-строку.
     * Проверяет формат {@link LocalDateTime} и сериализацию {@link BookingStatus}.
     *
     * @throws IOException если произошла ошибка при сериализации.
     */
    @Test
    void testSerialize() throws IOException {
        // 1. Подготовка: Создание DTO с фиксированными значениями
        LocalDateTime startDateTime = LocalDateTime.of(2025, 12, 10, 12, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 12, 11, 13, 15, 0);
        BookingDto dto = new BookingDto(
                1L,
                startDateTime,
                endDateTime,
                100L,
                200L,
                BookingStatus.APPROVED // Проверяем Enum
        );

        // Ожидаемые строковые представления дат
        String expectedStartJson = "2025-12-10T12:00:00";
        String expectedEndJson = "2025-12-11T13:15:00";

        // 2. Действие: Сериализация DTO
        JsonContent<BookingDto> result = this.jacksonTester.write(dto);

        // 3. Проверка: Проверяем поля JSON
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(100);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(200);

        // Проверка формата LocalDateTime
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(expectedStartJson);
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(expectedEndJson);

        // Проверка сериализации Enum (по умолчанию в виде строки)
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    /**
     * Тестирование корректности десериализации объекта {@link BookingDto} из JSON-строки.
     * Проверяет, что {@link LocalDateTime} и {@link BookingStatus} корректно восстанавливаются.
     *
     * @throws IOException если произошла ошибка при десериализации.
     */
    @Test
    void testDeserialize() throws IOException {
        // 1. Подготовка: JSON-строка и ожидаемый DTO
        String jsonContent = "{\"id\": 5, \"start\": \"2026-01-01T09:00:00\", \"end\": \"2026-01-02T10:00:00\", \"itemId\": 50, \"bookerId\": 60, \"status\": \"WAITING\"}";

        LocalDateTime expectedStart = LocalDateTime.of(2026, 1, 1, 9, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(2026, 1, 2, 10, 0, 0);

        BookingDto expectedDto = new BookingDto(
                5L,
                expectedStart,
                expectedEnd,
                50L,
                60L,
                BookingStatus.WAITING
        );

        // 2. Действие: Десериализация JSON
        BookingDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // 3. Проверка: Сравнение полученного объекта с ожидаемым
        assertThat(resultDto).isEqualTo(expectedDto);

        // Дополнительная проверка полей LocalDateTime и Enum
        assertThat(resultDto.start()).isEqualTo(expectedStart);
        assertThat(resultDto.end()).isEqualTo(expectedEnd);
        assertThat(resultDto.status()).isEqualTo(BookingStatus.WAITING);
    }
}