package com.avaneesh.yodha.Eventify.mapper;

import com.avaneesh.yodha.Eventify.dto.request.EventRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.EventsResponse;
import com.avaneesh.yodha.Eventify.entities.Events;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "bookedSeats", constant = "0") // Set initially to 0
    Events toEvent(EventRequestDTO eventRequestDTO);

    EventsResponse toEventResponse(Events event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seats", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "bookedSeats", ignore = true)
    void updateEventFromDto(EventRequestDTO dto, @MappingTarget Events entity);
}
