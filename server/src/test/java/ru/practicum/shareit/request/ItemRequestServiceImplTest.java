package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
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

    @Test
    void createRequest_userNotFound_throwsException() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Need something");
        assertThrows(NotFoundException.class, () -> requestService.create(999L, dto));
    }

    @Test
    void createRequest_emptyDescription_success() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("");
        ItemRequestResponseDto result = requestService.create(user.getId(), dto);

        assertNotNull(result.id());
        assertEquals("", result.description());
        assertNotNull(result.created());
    }

    @Test
    void getAllByRequestor_noRequests_returnsEmptyList() {
        List<ItemRequestResponseDto> result = requestService.getAllByRequestor(user.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllByRequestor_multipleRequests_sortedByCreatedDesc() {
        LocalDateTime now = LocalDateTime.now();
        ItemRequest req1 = new ItemRequest(null, "First", user, now.minusHours(2));
        ItemRequest req2 = new ItemRequest(null, "Second", user, now.minusHours(1));
        requestRepository.saveAll(List.of(req1, req2));

        List<ItemRequestResponseDto> result = requestService.getAllByRequestor(user.getId());

        assertEquals(2, result.size());
        assertEquals("Second", result.get(0).description());
        assertEquals("First", result.get(1).description());
    }

    @Test
    void getAllByRequestor_withItems_attachedCorrectly() {
        ItemRequest request = requestRepository.save(new ItemRequest(null, "Need drill", user, LocalDateTime.now()));
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        Item item = itemRepository.save(new Item(null, "Drill", "Power drill", true, owner, request));

        List<ItemRequestResponseDto> result = requestService.getAllByRequestor(user.getId());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).items().size());
        assertEquals(item.getId(), result.get(0).items().get(0).id());
        assertEquals("Drill", result.get(0).items().get(0).name());
    }

    @Test
    void getAllByRequestor_userNotFound_throwsException() {
        assertThrows(NotFoundException.class, () -> requestService.getAllByRequestor(999L));
    }

    @Test
    void getAll_noRequests_returnsEmptyList() {
        List<ItemRequestResponseDto> result = requestService.getAll(user.getId(), 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_excludesOwnRequests() {
        ItemRequest own = requestRepository.save(new ItemRequest(null, "My request", user, LocalDateTime.now()));
        User other = userRepository.save(new User(null, "Other", "other@example.com"));
        ItemRequest foreign = requestRepository.save(new ItemRequest(null, "Foreign request", other, LocalDateTime.now()));

        List<ItemRequestResponseDto> result = requestService.getAll(user.getId(), 0, 10);

        assertEquals(1, result.size());
        assertEquals("Foreign request", result.get(0).description());
    }

    @Test
    void getAll_pagination_worksCorrectly() {
        User other = userRepository.save(new User(null, "Other", "other@example.com"));
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 5; i++) {
            requestRepository.save(new ItemRequest(null, "Request " + i, other, now.minusMinutes(i)));
        }

        List<ItemRequestResponseDto> page1 = requestService.getAll(user.getId(), 0, 2);
        List<ItemRequestResponseDto> page2 = requestService.getAll(user.getId(), 2, 2);

        assertEquals(2, page1.size());
        assertEquals(2, page2.size());
        assertEquals("Request 0", page1.get(0).description());
        assertEquals("Request 2", page2.get(0).description());
    }

    @Test
    void getAll_fromExceedsTotal_returnsEmptyList() {
        User other = userRepository.save(new User(null, "Other", "other@example.com"));
        requestRepository.save(new ItemRequest(null, "One", other, LocalDateTime.now()));

        List<ItemRequestResponseDto> result = requestService.getAll(user.getId(), 10, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_withItems_attachedToCorrectRequests() {
        User other1 = userRepository.save(new User(null, "User1", "u1@example.com"));
        User other2 = userRepository.save(new User(null, "User2", "u2@example.com"));
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));

        ItemRequest req1 = requestRepository.save(new ItemRequest(null, "Need saw", other1, LocalDateTime.now()));
        ItemRequest req2 = requestRepository.save(new ItemRequest(null, "Need hammer", other2, LocalDateTime.now()));

        itemRepository.save(new Item(null, "Saw", "Hand saw", true, owner, req1));
        itemRepository.save(new Item(null, "Hammer", "Claw hammer", true, owner, req2));

        List<ItemRequestResponseDto> result = requestService.getAll(user.getId(), 0, 10);

        assertEquals(2, result.size());
        ItemRequestResponseDto dto1 = result.stream()
                .filter(r -> r.description().equals("Need saw"))
                .findFirst().orElseThrow();
        ItemRequestResponseDto dto2 = result.stream()
                .filter(r -> r.description().equals("Need hammer"))
                .findFirst().orElseThrow();

        assertEquals(1, dto1.items().size());
        assertEquals("Saw", dto1.items().get(0).name());
        assertEquals(1, dto2.items().size());
        assertEquals("Hammer", dto2.items().get(0).name());
    }

    @Test
    void getAll_userNotFound_throwsException() {
        assertThrows(NotFoundException.class, () -> requestService.getAll(999L, 0, 10));
    }

    @Test
    void getById_requestNotFound_throwsException() {
        assertThrows(NotFoundException.class, () -> requestService.getById(user.getId(), 999L));
    }

    @Test
    void getById_userNotFound_throwsException() {
        ItemRequest request = requestRepository.save(new ItemRequest(null, "Test", user, LocalDateTime.now()));
        assertThrows(NotFoundException.class, () -> requestService.getById(999L, request.getId()));
    }

    @Test
    void getById_withItems_returnsAttachedItems() {
        ItemRequest request = requestRepository.save(new ItemRequest(null, "Need drill", user, LocalDateTime.now()));
        User owner = userRepository.save(new User(null, "Owner", "owner@example.com"));
        Item item = itemRepository.save(new Item(null, "Drill", "Cordless", true, owner, request));

        ItemRequestResponseDto result = requestService.getById(user.getId(), request.getId());

        assertEquals(1, result.items().size());
        assertEquals(item.getId(), result.items().get(0).id());
        assertEquals("Drill", result.items().get(0).name());
    }

    @Test
    void getById_noItems_returnsEmptyItemsList() {
        ItemRequest request = requestRepository.save(new ItemRequest(null, "Need something", user, LocalDateTime.now()));

        ItemRequestResponseDto result = requestService.getById(user.getId(), request.getId());

        assertTrue(result.items().isEmpty());
    }

    @Test
    void mapToDtoWithItems_emptyList_returnsEmpty() {
        // Косвенно тестируется через getAllByRequestor_noRequests, но можно и явно
        List<ItemRequestResponseDto> result = requestService.getAllByRequestor(user.getId());
        assertTrue(result.isEmpty());
    }
}