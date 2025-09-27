package com.avaneesh.yodha.Eventify.dto.response;

import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Users;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponse {
    private Long id;
    private LocalDateTime bookingTimestamp;
    private Double totalAmount;
    private BookingStatus status;
    private Users user;
    private Events event;
    private List<SeatsResponse> bookedSeats;
}
