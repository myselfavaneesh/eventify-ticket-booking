package com.avaneesh.yodha.Eventify.mapper;

import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.entities.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring", uses = {SeatMapper.class}) // uses is for nested mappings
public interface BookingMapper {
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "user", ignore = true)
    BookingResponse toBookingResponse(Booking booking);

    List<BookingResponse> toBookingResponseList(List<Booking> bookings);

}