package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректности JSON-сериализации и десериализации DTO ItemDto.
 * Проверяется маппинг стандартных полей, включая булево поле 'available'.
 */
@JsonTest
@ActiveProfiles("test")
public class ItemDtoTest {

    /**
     * Джексон-тестер для {@link ItemDto}.
     */
    @Autowired
    private JacksonTester<ItemDto> jacksonTester;

    /**
     * Тестирование корректности сериализации объекта {@link ItemDto} в JSON-строку.
     * Проверяет, что все поля, включая булево 'available', корректно маппятся.
     *
     * @throws IOException если произошла ошибка при сериализации.
     */
    @Test
    void testSerialize() throws IOException {
        // 1. Подготовка (Arrange): Создание DTO с фиксированными значениями
        ItemDto dto = new ItemDto(
                1L,
                "Кресло-мешок",
                "Удобное кресло для отдыха",
                true, // Проверяемое булево поле
                10L
        );

        // 2. Действие (Act): Сериализация DTO
        JsonContent<ItemDto> result = this.jacksonTester.write(dto);

        // 3. Проверка (Assert): Проверяем поля JSON
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Кресло-мешок");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Удобное кресло для отдыха");

        // Проверка булевого поля 'available'
        assertThat(result).extractingJsonPathBooleanValue("$.available")
                .as("Проверка сериализации булевого поля 'available'")
                .isTrue();

        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(10);
    }

    /**
     * Тестирование корректности десериализации объекта {@link ItemDto} из JSON-строки.
     * Проверяет, что все поля, включая булево 'available', корректно восстанавливаются.
     *
     * @throws IOException если произошла ошибка при десериализации.
     */
    @Test
    void testDeserialize() throws IOException {
        // 1. Подготовка (Arrange): JSON-строка для недоступной вещи
        String jsonContent = "{\"id\": 2, \"name\": \"Лазер\", \"description\": \"Мощный лазерный уровень\", \"available\": false, \"requestId\": 11}";

        ItemDto expectedDto = new ItemDto(
                2L,
                "Лазер",
                "Мощный лазерный уровень",
                false, // Проверяемое булево поле
                11L
        );

        // 2. Действие (Act): Десериализация JSON
        ItemDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // 3. Проверка (Assert): Сравнение полученного объекта с ожидаемым
        assertThat(resultDto)
                .as("Проверка полного соответствия десериализованного объекта")
                .isEqualTo(expectedDto);

        // Дополнительная проверка булевого поля
        assertThat(resultDto.available())
                .as("Проверка десериализации булевого поля 'available'")
                .isFalse();
    }
}