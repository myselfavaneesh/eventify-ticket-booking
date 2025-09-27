package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.request.PaymentRequest;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.PaymentResponse;
import com.avaneesh.yodha.Eventify.enums.PaymentStatus;
import com.avaneesh.yodha.Eventify.services.PaymentService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(@PathVariable Long bookingId) {
        PaymentResponse paymentResponse = paymentService.initiatePayment(bookingId);
        ApiResponse<PaymentResponse> response = new ApiResponse<>(true, "Payment initiated successfully.", paymentResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(@Valid PaymentRequest paymentRequest) {
        BookingResponse bookingResponse;
        ApiResponse<BookingResponse> apiResponse;

        if (paymentRequest.getPaymentStatus() == PaymentStatus.COMPLETED) {
            bookingResponse = paymentService.confirmPayment(paymentRequest);
            apiResponse = new ApiResponse<>(true, "Payment confirmed and booking is complete.", bookingResponse);
        } else {
            bookingResponse = paymentService.rejectPayment(paymentRequest);
            apiResponse = new ApiResponse<>(false, "Payment Failed and booking is Canceled.", bookingResponse);
        }
        return ResponseEntity.ok(apiResponse);
    }
}