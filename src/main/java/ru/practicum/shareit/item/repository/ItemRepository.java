package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Item}.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Возвращает список всех вещей, принадлежащих указанному владельцу.
     *
     * @param ownerId Идентификатор владельца.
     * @param pageable Параметры пагинации.
     * @return Список вещей.
     */
    List<Item> findAllByOwnerIdOrderById(Long ownerId, Pageable pageable);

    /**
     * Ищет вещи по названию или описанию, не зависимо от регистра, среди доступных вещей.
     *
     * @param text Текст для поиска.
     * @param pageable Параметры пагинации.
     * @return Список найденных вещей.
     */
    @Query("SELECT i FROM Item i " +
           "WHERE i.available = TRUE AND " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> search(@Param("text") String text, Pageable pageable);

    /**
     * Возвращает список вещей, связанных с указанным запросом.
     *
     * @param requestId ID запроса.
     * @return Список вещей.
     */
    List<Item> findAllByRequestId(Long requestId);
}