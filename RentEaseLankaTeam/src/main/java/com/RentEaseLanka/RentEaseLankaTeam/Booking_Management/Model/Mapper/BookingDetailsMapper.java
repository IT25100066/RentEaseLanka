package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Mapper;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingDetailsMapper {

    @Mapping(source = "vehicle.model", target = "model")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "user.id", target = "customerId")
    @Mapping(target = "customerName", expression = "java(booking.getUser() != null ? ((booking.getUser().getFirstName() != null ? booking.getUser().getFirstName() : \"\") + \" \" + (booking.getUser().getLastName() != null ? booking.getUser().getLastName() : \"\")).trim() : null)")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(target = "driverName", expression = "java(booking.getDriver() != null ? ((booking.getDriver().getFirstName() != null ? booking.getDriver().getFirstName() : \"\") + \" \" + (booking.getDriver().getLastName() != null ? booking.getDriver().getLastName() : \"\")).trim() : null)")
    BookingDetailsDTO toDto(Booking booking);
}
