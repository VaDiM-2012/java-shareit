package ru.practicum.shareit.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.comment.model.Comment;


import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Comment}.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Находит все комментарии для указанной вещи.
     * @param itemId ID вещи.
     * @return Список комментариев.
     */
    List<Comment> findAllByItemId(Long itemId);

    /**
     * Находит все комментарии для списка вещей.
     * @param itemIds Список ID вещей.
     * @return Список комментариев.
     */
    List<Comment> findAllByItemIdIn(List<Long> itemIds);
}