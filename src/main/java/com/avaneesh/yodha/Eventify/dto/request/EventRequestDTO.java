package com.avaneesh.yodha.Eventify.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;


@Data
public class EventRequestDTO {
    @NotBlank(message = "Event name cannot be blank")
    @Size(min = 3, max = 100, message = "Event name must be between 3 and 100 characters")
    private String name;
    @NotBlank(message = "Event description cannot be blank")
    @Size(min = 10, max = 500, message = "Event description must be between 10 and 500 characters")
    private String description;
    @NotBlank(message = "Venue cannot be blank")
    @Size(min = 3, max = 100, message = "Venue must be between 3 and 100 characters")
    private String venue;
    @NotBlank(message = "Event timestamp cannot be blank")
    private String eventTimestamp;
    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;
    @Min(value = 1, message = "Seats per row must be at least 1")
    private int seatsPerRow;
    @NotEmpty(message = "Seat pricing cannot be empty")
    private List<Double> seatPricing;
}
