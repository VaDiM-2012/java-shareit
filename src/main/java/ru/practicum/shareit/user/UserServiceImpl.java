package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

/**
 * Имплементация сервиса для работы с сущностью {@link User}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        try {
            User savedUser = userRepository.save(user);
            log.info("Создан пользователь: {}", savedUser.getEmail());
            return UserMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при создании пользователя с email {}: {}", userDto.email(), e.getMessage());
            // В идеале, нужно создать кастомное исключение с кодом 409 CONFLICT
            throw new UnsupportedOperationException("Email уже существует");
        }
    }

    @Override
    public UserDto getById(Long userId) {
        User user = findUserById(userId);
        log.info("Получен пользователь с ID: {}", userId);
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        log.info("Получен список всех пользователей. Количество: {}", users.size());
        return UserMapper.toDto(users);
    }

    @Transactional
    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User existingUser = findUserById(userId);

        if (userDto.name() != null) {
            existingUser.setName(userDto.name());
        }
        if (userDto.email() != null) {
            existingUser.setEmail(userDto.email());
        }

        try {
            User updatedUser = userRepository.save(existingUser);
            log.info("Обновлен пользователь с ID {}: {}", userId, updatedUser.getEmail());
            return UserMapper.toDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка при обновлении пользователя с ID {}: Email уже существует", userId);
            throw new UnsupportedOperationException("Email уже существует");
        }
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
        log.info("Удален пользователь с ID: {}", userId);
    }

    /**
     * Вспомогательный метод для поиска пользователя и обработки NotFound.
     * @param userId ID пользователя.
     * @return Объект User.
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));
    }
}