package com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Mapper;

import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.Booking;
import com.RentEaseLanka.RentEaseLankaTeam.Booking_Management.Model.DTOs.BookingDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import javax.xml.transform.Source;

@Mapper(componentModel = "spring")
public interface BookingDetailsMapper {

    @Mapping(source = "vehicle.model" , target = "model")
    BookingDetailsDTO toDto (Booking booking);


}
