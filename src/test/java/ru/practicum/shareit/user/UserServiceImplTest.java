package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUser_success() {
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");
        UserDto createdUser = userService.create(userDto);

        assertNotNull(createdUser.id());
        assertEquals("John Doe", createdUser.name());
        assertEquals("john@example.com", createdUser.email());
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        UserDto userDto1 = new UserDto(null, "John Doe", "john@example.com");
        userService.create(userDto1);

        UserDto userDto2 = new UserDto(null, "Jane Doe", "john@example.com");
        assertThrows(Exception.class, () -> userService.create(userDto2));
    }

    @Test
    void getById_success() {
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = userRepository.save(user);

        UserDto userDto = userService.getById(savedUser.getId());
        assertEquals(savedUser.getId(), userDto.id());
        assertEquals("John Doe", userDto.name());
        assertEquals("john@example.com", userDto.email());
    }

    @Test
    void getById_notFound_throwsException() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAll_success() {
        userRepository.save(new User(null, "John Doe", "john@example.com"));
        userRepository.save(new User(null, "Jane Doe", "jane@example.com"));

        List<UserDto> users = userService.getAll();
        assertEquals(2, users.size());
    }

    @Test
    void updateUser_success() {
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = userRepository.save(user);

        UserDto updatedDto = new UserDto(null, "John Updated", "john.updated@example.com");
        UserDto updatedUser = userService.update(savedUser.getId(), updatedDto);

        assertEquals(savedUser.getId(), updatedUser.id());
        assertEquals("John Updated", updatedUser.name());
        assertEquals("john.updated@example.com", updatedUser.email());
    }

    @Test
    void deleteUser_success() {
        User user = new User(null, "John Doe", "john@example.com");
        User savedUser = userRepository.save(user);

        userService.delete(savedUser.getId());
        assertFalse(userRepository.existsById(savedUser.getId()));
    }
}