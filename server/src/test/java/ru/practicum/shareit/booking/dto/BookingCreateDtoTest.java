package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректности JSON-сериализации и десериализации
 * Data Transfer Object (DTO) BookingCreateDto.
 * Использует аннотацию @JsonTest для инициализации специализированного контекста Jackson.
 */
@JsonTest
@ActiveProfiles("test") // Установка активного профиля для теста
public class BookingCreateDtoTest {

    /**
     * {@link JacksonTester} для тестируемого DTO.
     * Автоматически инициализируется Spring Boot при использовании @JsonTest.
     */
    @Autowired
    private JacksonTester<BookingCreateDto> jacksonTester;

    /**
     * Тестирование корректности сериализации объекта {@link BookingCreateDto} в JSON-строку.
     * Проверяет, что поля {@link LocalDateTime} сериализуются в стандартном ISO-формате
     * (yyyy-MM-dd'T'HH:mm:ss), который является поведением Jackson по умолчанию для Java 8 Time API.
     *
     * @throws IOException если произошла ошибка при сериализации.
     */
    @Test
    void testSerialize() throws IOException {
        // 1. Подготовка (Arrange): Задаем фиксированные значения для DTO
        LocalDateTime startDateTime = LocalDateTime.of(2025, 10, 25, 10, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2025, 10, 26, 11, 30, 0);
        Long itemId = 1L;

        BookingCreateDto dto = new BookingCreateDto(itemId, startDateTime, endDateTime);

        // Ожидаемые строковые представления дат в JSON
        String expectedStartJson = "2025-10-25T10:00:00";
        String expectedEndJson = "2025-10-26T11:30:00";

        // 2. Действие (Act): Выполняем сериализацию DTO
        JsonContent<BookingCreateDto> result = this.jacksonTester.write(dto);

        // 3. Проверка (Assert): Проверяем, что JSON-поля соответствуют ожидаемым значениям и формату
        // Проверка поля itemId
        assertThat(result)
                .extractingJsonPathNumberValue("$.itemId")
                .as("Проверка поля itemId")
                .isEqualTo(itemId.intValue());

        // Проверка корректности формата сериализации поля start (LocalDateTime)
        assertThat(result)
                .extractingJsonPathStringValue("$.start")
                .as("Проверка формата сериализации даты начала (start)")
                .isEqualTo(expectedStartJson);

        // Проверка корректности формата сериализации поля end (LocalDateTime)
        assertThat(result)
                .extractingJsonPathStringValue("$.end")
                .as("Проверка формата сериализации даты окончания (end)")
                .isEqualTo(expectedEndJson);
    }

    // ---------------------------------------------------------------------------------------------------

    /**
     * Тестирование корректности десериализации объекта {@link BookingCreateDto} из JSON-строки.
     * Проверяет, что JSON-строка с датами в формате yyyy-MM-dd'T'HH:mm:ss корректно
     * преобразуется обратно в объект Java, восстанавливая {@link LocalDateTime} без потери данных.
     *
     * @throws IOException если произошла ошибка при десериализации.
     */
    @Test
    void testDeserialize() throws IOException {
        // 1. Подготовка (Arrange): Создаем JSON-строку и ожидаемый DTO-объект
        Long expectedItemId = 2L;
        LocalDateTime expectedStart = LocalDateTime.of(2025, 11, 1, 15, 0, 0);
        LocalDateTime expectedEnd = LocalDateTime.of(2025, 11, 2, 16, 45, 0);

        // JSON-строка, соответствующая ожидаемому формату
        String jsonContent = "{\"itemId\": 2, \"start\": \"2025-11-01T15:00:00\", \"end\": \"2025-11-02T16:45:00\"}";

        // Ожидаемый DTO-объект
        BookingCreateDto expectedDto = new BookingCreateDto(expectedItemId, expectedStart, expectedEnd);

        // 2. Действие (Act): Выполняем десериализацию JSON-строки
        BookingCreateDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // 3. Проверка (Assert): Сравниваем полученный DTO-объект с ожидаемым
        assertThat(resultDto)
                .as("Проверка полного соответствия десериализованного объекта")
                .isEqualTo(expectedDto);

        // Дополнительная детальная проверка полей LocalDateTime
        assertThat(resultDto.itemId())
                .as("Проверка поля itemId после десериализации")
                .isEqualTo(expectedItemId);
        assertThat(resultDto.start())
                .as("Проверка поля start (LocalDateTime) после десериализации")
                .isEqualTo(expectedStart);
        assertThat(resultDto.end())
                .as("Проверка поля end (LocalDateTime) после десериализации")
                .isEqualTo(expectedEnd);
    }
}