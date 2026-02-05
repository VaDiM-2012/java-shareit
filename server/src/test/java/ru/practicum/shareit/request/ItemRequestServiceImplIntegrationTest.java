package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Интеграционные тесты для сервиса запросов вещей, проверяющие взаимодействие с базой данных.
 * Используется профиль "test" для работы с тестовой базой данных.
 */
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    /**
     * Тест проверяет успешное создание запроса вещи и его корректное сохранение в базе данных.
     */
    @Test
    void create_shouldSaveItemRequestInDb() {
        // ARRANGE: Создание и сохранение пользователя-запрашивающего (requester).
        User requester = saveUser("Requester", "requester@mail.com");
        String description = "Нужна мощная дрель для ремонта!";

        ItemRequestCreateDto requestDto = new ItemRequestCreateDto(description);

        // ACT: Вызов тестируемого метода сервиса
        // Используем id() для Record DTO
        ItemRequestResponseDto createdRequestDto = itemRequestService.create(requester.getId(), requestDto);

        // ASSERT: Проверка результата вызова и состояния базы данных.
        Optional<ItemRequest> savedRequest = itemRequestRepository.findById(createdRequestDto.id());

        assertTrue(savedRequest.isPresent(), "Запрос должен быть сохранен в БД.");
        assertEquals(description, savedRequest.get().getDescription(), "Описание запроса должно совпадать.");

        // Используем getRequestor() на основе предоставленной сущности ItemRequest (Lombok).
        assertEquals(requester.getId(), savedRequest.get().getRequestor().getId(), "ID запрашивающего должен совпадать.");
        assertNotNull(savedRequest.get().getCreated(), "Дата создания должна быть установлена.");
    }

    /**
     * Тест проверяет получение всех запросов, созданных другими пользователями, с использованием пагинации.
     * Проверяет, что запросы запрашивающего не попадают в список.
     */
    @Test
    void getAllOthers_shouldReturnCorrectPageAndExcludeRequesterItems() {
        // ARRANGE: Создание пользователей и нескольких запросов.
        User requester = saveUser("Requester", "requester2@mail.com"); // Тот, кто запрашивает все чужие
        User owner1 = saveUser("Owner1", "owner1@mail.com");
        User owner2 = saveUser("Owner2", "owner2@mail.com");

        // Запрос запрашивающего (должен быть исключен)
        itemRequestRepository.save(new ItemRequest(null, "Мой запрос", requester, LocalDateTime.now().minusMinutes(1)));

        // Чужие запросы (должны быть возвращены)
        itemRequestRepository.save(new ItemRequest(null, "Чужой запрос 1", owner1, LocalDateTime.now().minusMinutes(3)));
        ItemRequest secondOthersRequest = itemRequestRepository.save(new ItemRequest(null, "Чужой запрос 2", owner2, LocalDateTime.now().minusMinutes(2)));

        // ACT: Получение чужих запросов с пагинацией (от 0, размер 10).
        // Используем метод itemRequestService.getAll(Long userId, int from, int size) из интерфейса.
        List<ItemRequestResponseDto> otherRequests = itemRequestService.getAll(requester.getId(), 0, 10);

        // ASSERT: Проверка размера списка и того, что запросы принадлежат другим пользователям.
        assertThat("Должны быть возвращены только чужие запросы.", otherRequests, hasSize(2));

        // ИСПРАВЛЕНО: Проверка отсутствия запроса requester в списке
        // Поскольку в ItemRequestResponseDto нет requesterId, мы проверяем, что запрос requester отсутствует в списке
        // по описанию или id, что косвенно подтверждает правильность фильтрации.

        // Проверяем, что в возвращенном списке нет запроса, который мы точно знаем, что принадлежит requester.
        boolean containsRequesterItem = otherRequests.stream()
                .anyMatch(dto -> dto.description().equals("Мой запрос"));

        assertFalse(containsRequesterItem, "Список не должен содержать запросы, созданные самим пользователем.");

        // Для дополнительной уверенности проверяем, что один из чужих запросов присутствует по id
        boolean containsOneOthersItem = otherRequests.stream()
                .anyMatch(dto -> dto.id().equals(secondOthersRequest.getId()));
        assertTrue(containsOneOthersItem, "В списке должен быть один из чужих запросов.");
    }
}