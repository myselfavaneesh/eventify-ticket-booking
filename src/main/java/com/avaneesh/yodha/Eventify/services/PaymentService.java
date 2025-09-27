package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.request.PaymentRequest;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.PaymentResponse;
import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.entities.Payments;
import com.avaneesh.yodha.Eventify.entities.Seat;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.enums.PaymentMethod;
import com.avaneesh.yodha.Eventify.enums.PaymentStatus;
import com.avaneesh.yodha.Eventify.enums.SeatStatus;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.mapper.BookingMapper;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import com.avaneesh.yodha.Eventify.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Transactional
    public PaymentResponse initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Payment can only be initiated for PENDING bookings.");
        }

        // 1. Create a new Payment record
        Payments newPayment = new Payments();
        newPayment.setBooking(booking);
        newPayment.setAmount(booking.getTotalAmount());
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setPaymentDate(LocalDateTime.now());
        newPayment.setTransactionId("txn_" + UUID.randomUUID().toString().replace("-", ""));

        // Save the payment record
        paymentRepository.save(newPayment);

        // 2. Create the mock payment URL
        String confirmationUrl = "http://localhost:8080/api/v1/payments/confirm?transactionId=" + newPayment.getTransactionId();

        return new PaymentResponse(confirmationUrl, "Payment initiated. use this link to complete the payment process");
    }

    @Transactional
    public BookingResponse confirmPayment(PaymentRequest paymentRequest) {
        // Find the payment by its transactionId, not the bookingId
        Payments payment = paymentRepository.findByTransactionId(paymentRequest.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found with id: " + paymentRequest.getTransactionId()));

        Booking booking = payment.getBooking();

        // 1. Update payment status
        payment.setStatus(paymentRequest.getPaymentStatus());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        paymentRepository.save(payment);

        // 2. Update booking status
        booking.setStatus(BookingStatus.CONFIRMED);

        // 3. Update seat status
        for (Seat seat : booking.getBookedSeats()) {
            seat.setStatus(SeatStatus.BOOKED);
        }

        Booking confirmedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(confirmedBooking);
    }

    @Transactional
    public BookingResponse rejectPayment(PaymentRequest paymentRequest) {
        Payments payment = paymentRepository.findByTransactionId(paymentRequest.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found with id: " + paymentRequest.getTransactionId()));

        Booking booking = payment.getBooking();

        // 1. Update payment status
        payment.setStatus(paymentRequest.getPaymentStatus());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        paymentRepository.save(payment);

        // 2. Update booking status
        booking.setStatus(BookingStatus.CANCELLED);

        // 3. Update seat status
        for (Seat seat : booking.getBookedSeats()) {
            seat.setStatus(SeatStatus.AVAILABLE);
        }

        Booking confirmedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(confirmedBooking);
    }

    @Transactional
    public void refundPayment(String transactionId){
        Payments payment = paymentRepository.findByTransactionId(transactionId).orElseThrow(()->
                new ResourceNotFoundException("Payment transaction not found with id: "+transactionId));

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
    }
}