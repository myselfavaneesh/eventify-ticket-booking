package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.BookingRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.services.BookingService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing event bookings.
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Endpoints for managing event bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a new booking for an event.
     *
     * @param userDetails       The details of the authenticated user making the booking.
     * @param bookingRequestDTO The details of the booking request.
     * @return A response entity containing the created booking's details.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        BookingResponse bookingResponse = bookingService.createBooking(userDetails.getUsername(), bookingRequestDTO);
        ApiResponse<BookingResponse> response = new ApiResponse<>(true, "Booking created successfully", bookingResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a specific booking by its ID.
     *
     * @param id The ID of the booking to retrieve.
     * @return A response entity containing the booking details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse bookingResponse = bookingService.getBookingById(id);
        ApiResponse<BookingResponse> response = new ApiResponse<>(true, "Booking retrieved successfully", bookingResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all bookings for the currently authenticated user.
     *
     * @param userDetails The details of the authenticated user.
     * @return A response entity containing a list of the user's bookings.
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookingsForUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<BookingResponse> bookingResponses = bookingService.getAllBookingsForUser(userDetails.getUsername());
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>(true, "Bookings for user retrieved successfully", bookingResponses);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a specific booking for the authenticated user.
     *
     * @param id          The ID of the booking to cancel.
     * @param userDetails The details of the authenticated user.
     * @return A response entity confirming the cancellation.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        BookingResponse bookingResponse = bookingService.cancelBooking(id, userDetails.getUsername());
        ApiResponse<BookingResponse> response = new ApiResponse<>(true, "Booking cancelled successfully", bookingResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific booking. This is intended for administrative use.
     *
     * @param id The ID of the booking to delete.
     * @return A response entity with no content, indicating successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build(); // Return 204 No Content
    }
}
