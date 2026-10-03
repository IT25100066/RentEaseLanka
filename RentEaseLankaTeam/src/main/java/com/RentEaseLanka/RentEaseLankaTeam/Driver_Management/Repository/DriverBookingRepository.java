package com.RentEaseLanka.RentEaseLankaTeam.Driver_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface DriverBookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByDriver_IdOrderByPickupDateTimeAsc(Long driverId);

    List<Booking> findByDriver_IdAndStatusInOrderByPickupDateTimeAsc(Long driverId, Collection<BookingStatus> statuses);

    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.driver.id = :driverId
              AND (:bookingId IS NULL OR b.BookingId <> :bookingId)
              AND b.status IN :blockingStatuses
              AND b.pickupDateTime < :returnDateTime
              AND b.returnDateTime > :pickupDateTime
            """)
    List<Booking> findOverlappingDriverBookings(@Param("driverId") Long driverId,
                                                @Param("bookingId") Long bookingId,
                                                @Param("pickupDateTime") LocalDateTime pickupDateTime,
                                                @Param("returnDateTime") LocalDateTime returnDateTime,
                                                @Param("blockingStatuses") Collection<BookingStatus> blockingStatuses);
}
