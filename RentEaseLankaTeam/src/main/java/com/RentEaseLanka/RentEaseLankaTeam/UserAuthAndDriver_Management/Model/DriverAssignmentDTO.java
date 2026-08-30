package com.RentEaseLanka.RentEaseLankaTeam.UserAuthAndDriver_Management.Model;

/**
 * DTO for receiving driver assignment requests from Staff/Admin.
 * Links a driver to a specific customer booking.
 */
public class DriverAssignmentDTO {

    private Long driverId;
    private Long bookingId;

    public DriverAssignmentDTO() {}

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
}