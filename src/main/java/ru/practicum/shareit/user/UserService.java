package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * Сервис для управления сущностью User.
 * Содержит бизнес-логику и координацию работы с репозиторием.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final InMemoryUserRepository userRepository;

    /**
     * Создает нового пользователя.
     *
     * @param userDto DTO с данными нового пользователя.
     * @return DTO созданного пользователя.
     */
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    /**
     * Обновляет данные пользователя по его ID. Реализует PATCH-логику,
     * обновляя только не-null поля в userDto.
     *
     * @param id ID пользователя для обновления.
     * @param userDto DTO с обновляемыми данными.
     * @return DTO обновленного пользователя.
     * @throws NotFoundException Если пользователь не найден.
     */
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id);

        // Логика обновления только не-null полей (PATCH)
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        // Сохранение, которое включает валидацию email
        return UserMapper.toUserDto(userRepository.save(existingUser));
    }

    /**
     * Находит пользователя по ID.
     *
     * @param id ID пользователя.
     * @return DTO найденного пользователя.
     * @throws NotFoundException Если пользователь не найден.
     */
    public UserDto findById(Long id) {
        return UserMapper.toUserDto(userRepository.findById(id));
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @return List DTO всех пользователей.
     */
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param id ID пользователя для удаления.
     */
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}