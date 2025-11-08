package ru.practicum.shareit.user;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    @Transactional
    UserDto create(UserDto userDto);

    UserDto getById(Long userId);

    List<UserDto> getAll();

    @Transactional
    UserDto update(Long userId, UserDto userDto);

    @Transactional
    void delete(Long userId);
}
