package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Repository;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
}
