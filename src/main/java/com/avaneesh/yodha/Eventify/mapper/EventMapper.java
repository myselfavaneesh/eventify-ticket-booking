package com.avaneesh.yodha.Eventify.mapper;

import com.avaneesh.yodha.Eventify.dto.request.EventRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.EventsResponse;
import com.avaneesh.yodha.Eventify.entities.Events;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;


@Mapper(componentModel = "spring" ,uses = {SeatMapper.class, BookingMapper.class})
public interface EventMapper {

    @Mapping(target = "seats" , ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "eventTimestamp" , expression = "java(java.time.LocalDateTime.parse(dto.getEventTimestamp()))")
    Events toEvent(EventRequestDTO dto);

    EventsResponse toEventResponse(Events event);

    default LocalDateTime map(String timestamp) {
        return LocalDateTime.parse(timestamp);
    }
}
