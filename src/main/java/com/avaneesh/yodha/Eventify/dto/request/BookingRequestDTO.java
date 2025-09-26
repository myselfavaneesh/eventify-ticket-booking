package com.avaneesh.yodha.Eventify.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequestDTO {
    @NotNull(message = "Event ID cannot be null")
    @Min(value = 1, message = "Event ID must be a positive number")
    private Long eventId;
    @NotNull(message = "User ID cannot be null")
    @Min(value = 1, message = "User ID must be a positive number")
    private Long userId;
    @NotEmpty(message = "Seat IDs cannot be empty")
    @NotNull(message = "Seat IDs cannot be null")
    private List<Long> seatIds;
}
