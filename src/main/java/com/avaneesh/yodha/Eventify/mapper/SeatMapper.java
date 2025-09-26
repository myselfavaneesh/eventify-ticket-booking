package com.avaneesh.yodha.Eventify.mapper;

import com.avaneesh.yodha.Eventify.dto.response.SeatsResponse;
import com.avaneesh.yodha.Eventify.entities.Seat;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    SeatsResponse toSeatsResponse(Seat seat);
    List<SeatsResponse> toSeatsResponseList(List<Seat> seats);
}