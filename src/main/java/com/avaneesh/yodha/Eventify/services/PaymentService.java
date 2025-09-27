package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.request.PaymentRequest;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.PaymentResponse;
import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Payments;
import com.avaneesh.yodha.Eventify.entities.Seat;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.enums.PaymentStatus;
import com.avaneesh.yodha.Eventify.enums.SeatStatus;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.mapper.BookingMapper;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import com.avaneesh.yodha.Eventify.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service layer for handling payment-related operations.
 */
@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;

    public PaymentService(BookingRepository bookingRepository, PaymentRepository paymentRepository, EventRepository eventRepository, BookingMapper bookingMapper, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.bookingMapper = bookingMapper;
        this.emailService = emailService;
    }

    /**
     * Initiates a payment for a PENDING booking.
     *
     * @param bookingId The ID of the booking.
     * @return A DTO containing the payment initiation details (e.g., a mock payment URL).
     */
    @Transactional
    public PaymentResponse initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Payment can only be initiated for PENDING bookings.");
        }

        if (booking.getPayment() != null) {
            throw new IllegalStateException("Payment has already been initiated for this booking.");
        }

        Payments newPayment = new Payments();
        newPayment.setBooking(booking);
        newPayment.setAmount(booking.getTotalAmount());
        newPayment.setStatus(PaymentStatus.PENDING);
        newPayment.setPaymentDate(LocalDateTime.now());
        newPayment.setTransactionId("txn_" + UUID.randomUUID().toString().replace("-", ""));

        paymentRepository.save(newPayment);
        booking.setPayment(newPayment);
        bookingRepository.save(booking);

        String confirmationUrl = "http://localhost:8080/api/v1/payments/webhook"; // Example webhook URL
        return new PaymentResponse(confirmationUrl, "Payment initiated. Use this link to complete the payment process.");
    }

    /**
     * Processes a payment confirmation webhook from a payment gateway.
     *
     * @param paymentRequest The payment details from the webhook.
     * @return A DTO representing the updated booking.
     */
    @Transactional
    public BookingResponse processPaymentWebhook(PaymentRequest paymentRequest) {
        Payments payment = paymentRepository.findByTransactionId(paymentRequest.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found with id: " + paymentRequest.getTransactionId()));

        if (paymentRequest.getPaymentStatus() == PaymentStatus.COMPLETED) {
            return handleSuccessfulPayment(payment, paymentRequest.getPaymentMethod());
        } else {
            return handleFailedPayment(payment, paymentRequest.getPaymentMethod());
        }
    }

    /**
     * Processes a refund for a given transaction.
     *
     * @param transactionId The unique ID of the transaction to refund.
     */
    @Transactional
    public void refundPayment(String transactionId) {
        Payments payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found with id: " + transactionId));

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
    }

    // --- Private Helper Methods ---

    private BookingResponse handleSuccessfulPayment(Payments payment, String paymentMethod) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod(paymentMethod);
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);

        for (Seat seat : booking.getBookedSeats()) {
            seat.setStatus(SeatStatus.BOOKED);
        }

        Booking confirmedBooking = bookingRepository.save(booking);
        emailService.sendBookingConfirmationEmail(confirmedBooking);
        return bookingMapper.toBookingResponse(confirmedBooking);
    }

    private BookingResponse handleFailedPayment(Payments payment, String paymentMethod) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setPaymentMethod(paymentMethod);
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CANCELLED);

        // Release the seats and update the event's booked seat count
        Events event = booking.getEvent();
        event.setBookedSeats(event.getBookedSeats() - booking.getNumberOfSeats());
        eventRepository.save(event);

        for (Seat seat : booking.getBookedSeats()) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null);
        }

        Booking failedBooking = bookingRepository.save(booking);
        emailService.sendPaymentFailedEmail(failedBooking);
        return bookingMapper.toBookingResponse(failedBooking);
    }
}
