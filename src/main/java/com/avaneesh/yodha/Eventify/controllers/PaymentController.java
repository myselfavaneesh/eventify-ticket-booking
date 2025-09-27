package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.PaymentRequest;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.PaymentResponse;
import com.avaneesh.yodha.Eventify.enums.PaymentStatus;
import com.avaneesh.yodha.Eventify.services.PaymentService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling payment processing for event bookings.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Endpoints for handling payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Initiates the payment process for a given booking.
     *
     * @param bookingId The ID of the booking for which to initiate payment.
     * @return A response entity containing the initial payment details.
     */
    @PostMapping("/initiate/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(@PathVariable Long bookingId) {
        PaymentResponse paymentResponse = paymentService.initiatePayment(bookingId);
        ApiResponse<PaymentResponse> response = new ApiResponse<>(true, "Payment initiated successfully.", paymentResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Handles the confirmation (or failure) of a payment, typically via a webhook from a payment gateway.
     *
     * @param paymentRequest The payment details from the gateway.
     * @return A response entity containing the updated booking status.
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<BookingResponse>> handlePaymentWebhook(@Valid @RequestBody PaymentRequest paymentRequest) {
        BookingResponse bookingResponse;
        ApiResponse<BookingResponse> apiResponse;

        if (paymentRequest.getPaymentStatus() == PaymentStatus.COMPLETED) {
            bookingResponse = paymentService.processPaymentWebhook(paymentRequest);
            apiResponse = new ApiResponse<>(true, "Payment confirmed and booking is complete.", bookingResponse);
        } else {
            bookingResponse = paymentService.processPaymentWebhook(paymentRequest);
            apiResponse = new ApiResponse<>(false, "Payment failed and booking is cancelled.", bookingResponse);
        }
        return ResponseEntity.ok(apiResponse);
    }
}
