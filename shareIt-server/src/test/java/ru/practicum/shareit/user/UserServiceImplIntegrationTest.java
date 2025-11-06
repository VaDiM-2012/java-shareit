package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для сервиса пользователей, проверяющие корректность обновления данных в базе данных.
 */
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }
    
    /**
     * Тест проверяет успешное обновление имени и email пользователя и корректное сохранение изменений в БД.
     */
    @Test
    void update_shouldUpdateUserFieldsInDb() {
        // ARRANGE: Создание и сохранение пользователя.
        User initialUser = saveUser("OldName", "old@mail.com");
        Long userId = initialUser.getId();

        // DTO с новыми данными
        UserDto updateDto = new UserDto(null, "NewName", "new.email@mail.com");

        // ACT: Вызов тестируемого метода
        userService.update(userId, updateDto);

        // ASSERT: Проверка состояния базы данных.
        Optional<User> updatedUserInDb = userRepository.findById(userId);

        assertTrue(updatedUserInDb.isPresent(), "Пользователь должен быть найден в БД после обновления.");
        assertEquals("NewName", updatedUserInDb.get().getName(), "Имя должно быть обновлено.");
        assertEquals("new.email@mail.com", updatedUserInDb.get().getEmail(), "Email должен быть обновлен.");
    }

    /**
     * Тест проверяет обновление только одного поля (например, имени) с сохранением старого email.
     */
    @Test
    void update_shouldUpdateOnlyNameField() {
        // ARRANGE: Создание и сохранение пользователя.
        User initialUser = saveUser("OldName", "keep@mail.com");
        Long userId = initialUser.getId();

        // DTO только с новым именем
        UserDto updateDto = new UserDto(null, "NewNameOnly", null);

        // ACT: Вызов тестируемого метода
        userService.update(userId, updateDto);

        // ASSERT: Проверка состояния базы данных.
        User updatedUserInDb = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        
        assertEquals("NewNameOnly", updatedUserInDb.getName(), "Имя должно быть обновлено.");
        assertEquals("keep@mail.com", updatedUserInDb.getEmail(), "Email должен остаться прежним.");
    }
}