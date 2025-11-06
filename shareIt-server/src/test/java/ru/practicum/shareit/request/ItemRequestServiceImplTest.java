package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private ItemRequestServiceImpl requestService;

    private User user;

    @BeforeEach
    void setUp() {
        requestService = new ItemRequestServiceImpl(requestRepository, userRepository, itemRepository);
        user = userRepository.save(new User(null, "User", "user@example.com"));
    }

    @Test
    void createRequest_success() {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto("Need a drill");
        ItemRequestResponseDto result = requestService.create(user.getId(), requestDto);

        assertNotNull(result.id());
        assertEquals("Need a drill", result.description());
        assertNotNull(result.created());
    }

    @Test
    void getAllByRequestor_success() {
        ItemRequest request = new ItemRequest(null, "Need a drill", user, LocalDateTime.now());
        requestRepository.save(request);

        List<ItemRequestResponseDto> requests = requestService.getAllByRequestor(user.getId());
        assertEquals(1, requests.size());
        assertEquals("Need a drill", requests.getFirst().description());
    }

    @Test
    void getAll_success() {
        User otherUser = userRepository.save(new User(null, "Other User", "other@example.com"));
        ItemRequest request = new ItemRequest(null, "Need a hammer", otherUser, LocalDateTime.now());
        requestRepository.save(request);

        List<ItemRequestResponseDto> requests = requestService.getAll(user.getId(), 0, 10);
        assertEquals(1, requests.size());
        assertEquals("Need a hammer", requests.getFirst().description());
    }

    @Test
    void getById_success() {
        ItemRequest request = new ItemRequest(null, "Need a drill", user, LocalDateTime.now());
        ItemRequest savedRequest = requestRepository.save(request);

        ItemRequestResponseDto result = requestService.getById(user.getId(), savedRequest.getId());
        assertEquals(savedRequest.getId(), result.id());
        assertEquals("Need a drill", result.description());
    }

    @Test
    void getById_notFound_throwsException() {
        assertThrows(NotFoundException.class, () -> requestService.getById(user.getId(), 999L));
    }
}