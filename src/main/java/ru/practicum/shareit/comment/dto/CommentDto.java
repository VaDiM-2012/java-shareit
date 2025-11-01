package ru.practicum.shareit.comment.dto;

import java.time.LocalDateTime;

/**
 * DTO для ответа о Комментарии.
 */
public record CommentDto(
        Long id,
        String text,
        String authorName,
        LocalDateTime created
) {}