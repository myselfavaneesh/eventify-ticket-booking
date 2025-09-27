package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.BookingRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.services.BookingService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody BookingRequestDTO bookingRequestDTO) {
        BookingResponse bookingResponse = bookingService.createBooking(userDetails.getUsername(), bookingRequestDTO);
        ApiResponse<BookingResponse> response = new ApiResponse<>(true, "Booking created successfully", bookingResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse bookingResponse = bookingService.getBookingById(id);
        ApiResponse<BookingResponse> response = new ApiResponse<>(true, "Booking retrieved successfully", bookingResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookingsForUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<BookingResponse> bookingResponses = bookingService.getAllBookingsForUser(userDetails.getUsername());
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>(true, "Bookings retrieved successfully", bookingResponses);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        BookingResponse bookingResponse = bookingService.cancelBooking(id,email);
        ApiResponse<BookingResponse> response = new ApiResponse<>(true, "Booking cancelled successfully", bookingResponse);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        ApiResponse<Void> response = new ApiResponse<>(true, "Booking deleted successfully", null);
        return ResponseEntity.ok(response);
    }

}
