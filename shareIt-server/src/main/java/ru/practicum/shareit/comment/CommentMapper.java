package ru.practicum.shareit.comment;

import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностью {@link Comment} и ее DTO.
 */
public class CommentMapper {

    private CommentMapper() {
        // Утилитарный класс
    }

    /**
     * Преобразует сущность {@link Comment} в DTO {@link CommentDto}.
     *
     * @param comment Сущность комментария.
     * @return DTO комментария.
     */
    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    /**
     * Преобразует коллекцию сущностей {@link Comment} в коллекцию DTO {@link CommentDto}.
     *
     * @param comments Список сущностей комментариев.
     * @return Список DTO комментариев.
     */
    public static List<CommentDto> toDto(List<Comment> comments) {
        return comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует DTO создания {@link CommentCreateDto} в сущность {@link Comment}.
     * ID, Item, Author и Created устанавливаются в Service.
     *
     * @param dto DTO создания комментария.
     * @return Сущность комментария.
     */
    public static Comment toEntity(CommentCreateDto dto) {
        Comment comment = new Comment();
        comment.setText(dto.text());
        return comment;
    }
}