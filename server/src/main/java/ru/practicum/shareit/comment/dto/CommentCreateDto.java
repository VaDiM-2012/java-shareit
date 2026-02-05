package ru.practicum.shareit.comment.dto;

/**
 * DTO для создания нового Комментария.
 */
public record CommentCreateDto(
        String text
) {}