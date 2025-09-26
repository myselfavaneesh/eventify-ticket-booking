package com.avaneesh.yodha.Eventify.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EventsResponse {
    private Long id;
    private String name;
    private String description;
    private String venue;
    private LocalDateTime eventTimestamp;
    private List<SeatsResponse> seats;
    private List<BookingResponse> bookings;
}
