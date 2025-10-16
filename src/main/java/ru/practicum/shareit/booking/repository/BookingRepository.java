package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Booking}.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // --- Методы для АРЕНДАТОРА (Booker) ---

    /**
     * Получает все бронирования арендатора.
     * @param bookerId ID арендатора.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    /**
     * Получает бронирования арендатора по статусу.
     * @param bookerId ID арендатора.
     * @param status Статус бронирования.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    /**
     * Получает текущие бронирования арендатора.
     * @param bookerId ID арендатора.
     * @param now Текущее время.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now, LocalDateTime end, Pageable pageable);

    /**
     * Получает будущие бронирования арендатора.
     * @param bookerId ID арендатора.
     * @param now Текущее время.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable pageable);

    /**
     * Получает завершенные бронирования арендатора.
     * @param bookerId ID арендатора.
     * @param now Текущее время.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable pageable);


    // --- Методы для ВЛАДЕЛЬЦА (Owner) ---

    /**
     * Получает все бронирования для вещей владельца.
     * @param ownerId ID владельца.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    /**
     * Получает бронирования вещей владельца по статусу.
     * @param ownerId ID владельца.
     * @param status Статус бронирования.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status, Pageable pageable);

    /**
     * Получает текущие бронирования вещей владельца.
     * @param ownerId ID владельца.
     * @param now Текущее время.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findAllCurrentByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Получает будущие бронирования вещей владельца.
     * @param ownerId ID владельца.
     * @param now Текущее время.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findAllFutureByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Получает завершенные бронирования вещей владельца.
     * @param ownerId ID владельца.
     * @param now Текущее время.
     * @param pageable Параметры пагинации.
     * @return Список бронирований.
     */
    @Query("SELECT b FROM Booking b JOIN b.item i WHERE i.owner.id = :ownerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findAllPastByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now, Pageable pageable);


    // --- Дополнительные методы для ItemService (Last/Next Booking) и CommentService (Past Booking Check) ---

    /**
     * Находит ближайшее завершенное бронирование для указанной вещи (last booking).
     * @param itemId ID вещи.
     * @param now Текущее время.
     * @param status Статус (APPROVED).
     * @return Optional с бронированием.
     */
    Optional<Booking> findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    /**
     * Находит ближайшее предстоящее бронирование для указанной вещи (next booking).
     * @param itemId ID вещи.
     * @param now Текущее время.
     * @param status Статус (APPROVED).
     * @return Optional с бронированием.
     */
    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    /**
     * Проверяет, существовало ли завершенное и подтвержденное бронирование вещи пользователем.
     * Используется для проверки права на добавление комментария.
     *
     * @param itemId ID вещи.
     * @param bookerId ID пользователя-арендатора.
     * @param now Текущее время.
     * @param status Статус (APPROVED).
     * @return Список бронирований (должен быть > 0).
     */
    List<Booking> findAllByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId, Long bookerId, BookingStatus status, LocalDateTime now);
}