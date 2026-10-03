package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.BookingStatus;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    List<Booking> findByUser_Id(Long userId);

    List<Booking> findByVehicle(Vehicle vehicle);

        @Query("""
                        SELECT COUNT(b)
                        FROM Booking b
                        WHERE b.status = :status
                            AND COALESCE(b.completedAt, b.returnDateTime) >= :from
                            AND COALESCE(b.completedAt, b.returnDateTime) < :to
                        """)
        long countRevenueBookings(@Param("status") BookingStatus status,
                                                            @Param("from") LocalDateTime from,
                                                            @Param("to") LocalDateTime to);

        @Query("""
                        SELECT COALESCE(SUM(b.totalAmount), 0)
                        FROM Booking b
                        WHERE b.status = :status
                            AND COALESCE(b.completedAt, b.returnDateTime) >= :from
                            AND COALESCE(b.completedAt, b.returnDateTime) < :to
                        """)
        java.math.BigDecimal sumRevenueBookings(@Param("status") BookingStatus status,
                                                                                        @Param("from") LocalDateTime from,
                                                                                        @Param("to") LocalDateTime to);
}
