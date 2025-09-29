package com.avaneesh.yodha.Eventify.dto.request;

import com.avaneesh.yodha.Eventify.enums.CategoryEnum;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private LocalDateTime eventTimestamp = LocalDateTime.now() ;
    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats ;
    @Min(value = 1, message = "Seats per row must be at least 1")
    private int seatsPerRow;
    @NotEmpty(message = "Seat pricing cannot be empty")
    private List<Double> seatPricing;
    @NotEmpty(message = "Images cannot be empty")
    @Size(min = 1,max = 5, message = "At least one image is required Or Maximum 5 Images are allowed")
    private List<MultipartFile> images;
    @NotNull(message = "Gender cannot be blank.")
    private CategoryEnum category;
}
