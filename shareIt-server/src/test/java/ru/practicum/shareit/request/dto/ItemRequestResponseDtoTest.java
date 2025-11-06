package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.dto.ItemDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректности JSON-сериализации и десериализации
 * DTO ItemRequestResponseDto. Особенность: проверка поля LocalDateTime и списка вложенных DTO.
 */
@JsonTest
@ActiveProfiles("test")
public class ItemRequestResponseDtoTest {

    /**
     * Джексон-тестер для {@link ItemRequestResponseDto}.
     */
    @Autowired
    private JacksonTester<ItemRequestResponseDto> jacksonTester;

    /**
     * Тестирование корректности сериализации объекта {@link ItemRequestResponseDto} в JSON-строку.
     * Проверяет формат {@link LocalDateTime} и структуру вложенного списка {@link ItemDto}.
     *
     * @throws IOException если произошла ошибка при сериализации.
     */
    @Test
    void testSerialize() throws IOException {
        // 1. Подготовка: Создание DTO с фиксированными значениями и вложенными объектами
        LocalDateTime createdDateTime = LocalDateTime.of(2025, 3, 15, 12, 12, 0);
        Long requestId = 10L;

        // Вложенные объекты ItemDto
        ItemDto item1 = new ItemDto(1L, "Дрель", "Мощная дрель", true, requestId);
        ItemDto item2 = new ItemDto(2L, "Пила", "Электрическая пила", true, requestId);
        List<ItemDto> items = List.of(item1, item2);

        ItemRequestResponseDto dto = new ItemRequestResponseDto(requestId, "Ищу инструменты для ремонта", createdDateTime, items);

        String expectedCreatedJson = "2025-03-15T12:12:00";

        // 2. Действие: Сериализация DTO
        JsonContent<ItemRequestResponseDto> result = this.jacksonTester.write(dto);

        // 3. Проверка: Проверяем поля JSON
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(requestId.intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ищу инструменты для ремонта");

        // Проверка формата LocalDateTime
        assertThat(result).extractingJsonPathStringValue("$.created").as("Проверка формата сериализации даты создания").isEqualTo(expectedCreatedJson);

        // Проверка вложенного списка items.
        // Используем .extractingJsonPathValue("$.items") для получения списка,
        // а затем AssertJ .asList().hasSize() для проверки его размера.
        assertThat(result).extractingJsonPathValue("$.items").asList().as("Проверка размера списка items").hasSize(2);
        // Проверка полей первого вложенного элемента
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(requestId.intValue());
    }

    /**
     * Тестирование корректности десериализации объекта {@link ItemRequestResponseDto} из JSON-строки.
     * Проверяет восстановление LocalDateTime и списка вложенных объектов.
     *
     * @throws IOException если произошла ошибка при десериализации.
     */
    @Test
    void testDeserialize() throws IOException {
        // 1. Подготовка: JSON-строка, содержащая вложенный список
        String jsonContent = """
                    {
                    "id": 20,
                    "description": "Срочно нужен ноутбук",
                    "created": "2025-04-01T15:00:00",
                    "items": [
                        {
                            "id": 5,
                            "name": "MacBook",
                            "description": "Новый ноутбук",
                            "available": true,
                            "requestId": 20
                        }
                    ]
                }
                """;

        LocalDateTime expectedCreated = LocalDateTime.of(2025, 4, 1, 15, 0, 0);

        // Ожидаемые DTO-объекты
        ItemDto expectedItem = new ItemDto(5L, "MacBook", "Новый ноутбук", true, 20L);
        ItemRequestResponseDto expectedDto = new ItemRequestResponseDto(20L, "Срочно нужен ноутбук", expectedCreated, List.of(expectedItem));

        // 2. Действие: Десериализация JSON
        ItemRequestResponseDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // 3. Проверка: Сравнение полученного объекта с ожидаемым
        assertThat(resultDto).as("Проверка полного соответствия десериализованного объекта").isEqualTo(expectedDto);

        // Дополнительная детальная проверка
        assertThat(resultDto.created()).isEqualTo(expectedCreated);
        assertThat(resultDto.items()).hasSize(1);
        assertThat(resultDto.items().getFirst().name()).isEqualTo("MacBook");
    }
}