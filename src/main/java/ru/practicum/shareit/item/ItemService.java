package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    @Transactional
    ItemDto create(Long ownerId, ItemDto itemDto);

    @Transactional
    ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

    ItemResponseDto getById(Long userId, Long itemId);

    List<ItemResponseDto> getAllByOwner(Long ownerId, int from, int size);

    List<ItemDto> search(String text, int from, int size);

    @Transactional
    CommentDto addComment(Long authorId, Long itemId, CommentCreateDto dto);
}
