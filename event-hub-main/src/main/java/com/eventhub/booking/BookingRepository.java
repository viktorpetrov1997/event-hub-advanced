package com.eventhub.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>
{
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("""
        SELECT COALESCE(SUM(b.seats), 0) FROM Booking b
        WHERE b.event.id = :eventId
      AND b.status <> com.eventhub.booking.BookingStatus.CANCELLED""")
    int sumBookedSeatsByEventId(@Param("eventId") UUID eventId);

    boolean existsByUserIdAndEventIdAndStatus(UUID userId, UUID eventId, BookingStatus status);

    boolean existsByEventIdAndStatusNot(UUID eventId, BookingStatus status);

    boolean existsByUserIdAndStatusAndEventEndTimeAfter(UUID userId, BookingStatus status, LocalDateTime now);

    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b
        WHERE b.event.organizer.id = :organizerId
          AND b.status = com.eventhub.booking.BookingStatus.CONFIRMED""")
    BigDecimal sumRevenueByOrganizerId(@Param("organizerId") UUID organizerId);

    void deleteAllByEventId(UUID eventId);
}