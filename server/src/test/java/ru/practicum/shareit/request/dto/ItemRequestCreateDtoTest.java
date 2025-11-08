package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тестовый класс для проверки корректности JSON-сериализации и десериализации
 * DTO ItemRequestCreateDto, содержащего только одно строковое поле.
 */
@JsonTest
@ActiveProfiles("test")
public class ItemRequestCreateDtoTest {

    /**
     * Джексон-тестер для {@link ItemRequestCreateDto}.
     */
    @Autowired
    private JacksonTester<ItemRequestCreateDto> jacksonTester;

    /**
     * Тестирование корректности сериализации объекта {@link ItemRequestCreateDto} в JSON-строку.
     * Проверяет, что поле 'description' корректно сериализуется.
     *
     * @throws IOException если произошла ошибка при сериализации.
     */
    @Test
    void testSerialize() throws IOException {
        // 1. Подготовка (Arrange): Создание DTO
        String description = "Ищу надежную палатку для похода в горы.";
        ItemRequestCreateDto dto = new ItemRequestCreateDto(description);

        // 2. Действие (Act): Сериализация DTO
        JsonContent<ItemRequestCreateDto> result = this.jacksonTester.write(dto);

        // 3. Проверка (Assert): Проверяем JSON-поле
        assertThat(result).extractingJsonPathStringValue("$.description").as("Проверка сериализации поля 'description'").isEqualTo(description);

        // Проверка, что нет других полей
        assertThat(result).extractingJsonPathMapValue("$").hasSize(1);
    }

    /**
     * Тестирование корректности десериализации объекта {@link ItemRequestCreateDto} из JSON-строки.
     * Проверяет, что строковое поле 'description' корректно восстанавливается.
     *
     * @throws IOException если произошла ошибка при десериализации.
     */
    @Test
    void testDeserialize() throws IOException {
        // 1. Подготовка (Arrange): JSON-строка
        String expectedDescription = "Нужен мощный ноутбук для работы.";
        String jsonContent = "{\"description\": \"Нужен мощный ноутбук для работы.\"}";

        ItemRequestCreateDto expectedDto = new ItemRequestCreateDto(expectedDescription);

        // 2. Действие (Act): Десериализация JSON
        ItemRequestCreateDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // 3. Проверка (Assert): Сравнение полученного объекта с ожидаемым
        assertThat(resultDto).as("Проверка полного соответствия десериализованного объекта").isEqualTo(expectedDto);
        // Дополнительная проверка поля
        assertThat(resultDto.description()).as("Проверка десериализации поля 'description'").isEqualTo(expectedDescription);
    }
}