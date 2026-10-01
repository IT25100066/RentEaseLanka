package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDetailsDTO {

    private LocalDateTime pickupDateTime;
    private String pickupLocation;
    private String model;
    private Long BookingId;




}
