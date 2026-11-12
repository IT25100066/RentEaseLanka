package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Mapper;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import com.RentEaseLanka.RentEaseLankaTeam.Fleet_And_Maintenance_Management.FleetManagement.Model.Vehicle;
import com.RentEaseLanka.RentEaseLankaTeam.User_Management.Model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-11-12T12:43:48+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.5 (Eclipse Adoptium)"
)
@Component
public class BookingDetailsMapperImpl implements BookingDetailsMapper {

    @Override
    public BookingDetailsDTO toDto(Booking booking) {
        if ( booking == null ) {
            return null;
        }

        BookingDetailsDTO bookingDetailsDTO = new BookingDetailsDTO();

        bookingDetailsDTO.setModel( bookingVehicleModel( booking ) );
        bookingDetailsDTO.setVehicleId( bookingVehicleId( booking ) );
        bookingDetailsDTO.setCustomerId( bookingUserId( booking ) );
        bookingDetailsDTO.setDriverId( bookingDriverId( booking ) );
        bookingDetailsDTO.setBookingId( booking.getBookingId() );
        bookingDetailsDTO.setPickupDateTime( booking.getPickupDateTime() );
        bookingDetailsDTO.setPickupLocation( booking.getPickupLocation() );
        bookingDetailsDTO.setReturnDateTime( booking.getReturnDateTime() );
        bookingDetailsDTO.setReturnLocation( booking.getReturnLocation() );
        bookingDetailsDTO.setStatus( booking.getStatus() );
        bookingDetailsDTO.setTotalAmount( booking.getTotalAmount() );
        bookingDetailsDTO.setWithDriver( booking.isWithDriver() );

        bookingDetailsDTO.setCustomerName( booking.getUser() != null ? ((booking.getUser().getFirstName() != null ? booking.getUser().getFirstName() : "") + " " + (booking.getUser().getLastName() != null ? booking.getUser().getLastName() : "")).trim() : null );
        bookingDetailsDTO.setDriverName( booking.getDriver() != null ? ((booking.getDriver().getFirstName() != null ? booking.getDriver().getFirstName() : "") + " " + (booking.getDriver().getLastName() != null ? booking.getDriver().getLastName() : "")).trim() : null );

        return bookingDetailsDTO;
    }

    private String bookingVehicleModel(Booking booking) {
        if ( booking == null ) {
            return null;
        }
        Vehicle vehicle = booking.getVehicle();
        if ( vehicle == null ) {
            return null;
        }
        String model = vehicle.getModel();
        if ( model == null ) {
            return null;
        }
        return model;
    }

    private Long bookingVehicleId(Booking booking) {
        if ( booking == null ) {
            return null;
        }
        Vehicle vehicle = booking.getVehicle();
        if ( vehicle == null ) {
            return null;
        }
        Long id = vehicle.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long bookingUserId(Booking booking) {
        if ( booking == null ) {
            return null;
        }
        User user = booking.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long bookingDriverId(Booking booking) {
        if ( booking == null ) {
            return null;
        }
        User driver = booking.getDriver();
        if ( driver == null ) {
            return null;
        }
        Long id = driver.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
