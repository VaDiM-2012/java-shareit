package ru.practicum.shareit.request;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    @Transactional
    ItemRequestResponseDto create(Long requestorId, ItemRequestCreateDto dto);

    List<ItemRequestResponseDto> getAllByRequestor(Long requestorId);

    List<ItemRequestResponseDto> getAll(Long userId, int from, int size);

    ItemRequestResponseDto getById(Long userId, Long requestId);
}
