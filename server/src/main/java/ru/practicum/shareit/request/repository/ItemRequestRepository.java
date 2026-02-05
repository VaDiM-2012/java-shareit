package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link ItemRequest}.
 */
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    /**
     * Возвращает список всех запросов, созданных указанным пользователем, отсортированных по дате создания.
     *
     * @param requestorId ID создателя запроса.
     * @return Список запросов.
     */
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);

    /**
     * Возвращает список всех запросов, кроме запросов указанного пользователя, с пагинацией.
     *
     * @param requestorId ID пользователя, чьи запросы нужно исключить.
     * @param pageable Параметры пагинации.
     * @return Список запросов.
     */
    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId, Pageable pageable);
}