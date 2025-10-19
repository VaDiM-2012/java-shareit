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
 * Репозиторий для бронирований.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Методы для арендатора
    List<Booking> findAllByBookerIdOrderByStartDesc(
            Long bookerId,
            Pageable pageable
    );

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(
            Long bookerId,
            BookingStatus status,
            Pageable pageable
    );

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime currentTime,
            LocalDateTime currentTimeAgain,
            Pageable pageable
    );

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime currentTime,
            Pageable pageable
    );

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId,
            LocalDateTime currentTime,
            Pageable pageable
    );

    // Методы для владельца
    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerId(
            @Param("ownerId") Long ownerId,
            Pageable pageable
    );

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") BookingStatus status,
            Pageable pageable
    );

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start < :currentTime " +
            "AND b.end > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findAllCurrentByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable
    );

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findAllFutureByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable
    );

    @Query("SELECT b " +
            "FROM Booking b " +
            "JOIN b.item i " +
            "WHERE i.owner.id = :ownerId " +
            "AND b.end < :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findAllPastByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable
    );

    // Дополнительные методы
    Optional<Booking> findFirstByItemIdAndStatusAndStartBeforeOrderByEndDesc(
            Long itemId,
            BookingStatus status,
            LocalDateTime currentTime
    );

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId,
            BookingStatus status,
            LocalDateTime currentTime
    );

    List<Booking> findAllByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime currentTime
    );
}