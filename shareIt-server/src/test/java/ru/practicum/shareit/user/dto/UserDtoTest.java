package ru.practicum.shareit.user.dto;

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
 * DTO UserDto, содержащего только стандартные типы.
 */
@JsonTest
@ActiveProfiles("test")
public class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> jacksonTester;

    /**
     * Тестирование сериализации: Проверка маппинга всех стандартных полей.
     * @throws IOException
     */
    @Test
    void testSerialize() throws IOException {
        // Подготовка
        UserDto dto = new UserDto(10L, "Иван", "ivan.ivanov@mail.ru");

        // Действие
        JsonContent<UserDto> result = this.jacksonTester.write(dto);

        // Проверка
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Иван");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan.ivanov@mail.ru");
    }

    /**
     * Тестирование десериализации: Проверка корректного восстановления стандартных полей.
     * @throws IOException
     */
    @Test
    void testDeserialize() throws IOException {
        // Подготовка
        String jsonContent = "{\"id\": 11, \"name\": \"Петр\", \"email\": \"petr.petrov@mail.ru\"}";

        UserDto expectedDto = new UserDto(11L, "Петр", "petr.petrov@mail.ru");

        // Действие
        UserDto resultDto = this.jacksonTester.parseObject(jsonContent);

        // Проверка
        assertThat(resultDto)
                .as("Проверка полного соответствия десериализованного объекта")
                .isEqualTo(expectedDto);

        assertThat(resultDto.id()).isEqualTo(11L);
        assertThat(resultDto.name()).isEqualTo("Петр");
    }
}