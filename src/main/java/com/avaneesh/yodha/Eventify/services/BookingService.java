package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.request.BookingRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Seat;
import com.avaneesh.yodha.Eventify.entities.Users;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.enums.SeatStatus;
import com.avaneesh.yodha.Eventify.exception.ResourceAlreadyExistsException;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.mapper.BookingMapper;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import com.avaneesh.yodha.Eventify.repository.SeatRepository;
import com.avaneesh.yodha.Eventify.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private PaymentService paymentService;

    @Transactional
    public BookingResponse createBooking(String email, BookingRequestDTO requestBooking) {

        Users reqUser = userRepository.getUsersByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Events event = eventRepository.findById(requestBooking.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + requestBooking.getEventId()));

        List<Seat> requestedSeats = seatRepository.findAllById(requestBooking.getSeatIds());

        LocalDateTime eventStartTime = event.getEventTimestamp();
        LocalDateTime bookingCutoffTime = eventStartTime.minusMinutes(15);

        // Check if the current time is after the booking cutoff time
        if (LocalDateTime.now().isAfter(bookingCutoffTime)) {
            throw new IllegalStateException("Booking is closed. Bookings cannot be made within 15 minutes of the event start time.");
        }

        // Check if all requested seats exist
        if (requestedSeats.size() != requestBooking.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more seats could not be found.");
        }

        // Check if any of the requested seats are already booked
        for (Seat seat : requestedSeats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new ResourceAlreadyExistsException("Seat with id " + seat.getId() + " is not available for booking.");
            }
        }
        // Calculate the total amount
        double totalAmount = requestedSeats.stream()
                .mapToDouble(Seat::getSeatPricing)
                .sum();

        // Create the booking object
        Booking newBooking = new Booking();
        newBooking.setUser(reqUser);
        newBooking.setEvent(event);
        newBooking.setBookingTimestamp(LocalDateTime.now());
        newBooking.setStatus(BookingStatus.PENDING); // Assuming payment will integrate later
        newBooking.setTotalAmount(totalAmount);

        // Update seat status and associate with the booking
        for (Seat seat : requestedSeats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setBooking(newBooking);
        }

        newBooking.setBookedSeats(requestedSeats);

        Booking savedBooking = bookingRepository.save(newBooking);

        return bookingMapper.toBookingResponse(savedBooking);
    }

    /**
     * READ: Retrieves a single booking by its ID.
     */
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return bookingMapper.toBookingResponse(booking);
    }

    /**
     * READ: Retrieves all bookings for a specific user.
     */
    public List<BookingResponse> getAllBookingsForUser(String email) {
        Users user = userRepository.getUsersByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        List<Booking> bookings = bookingRepository.findAllByUser(user);
        return bookingMapper.toBookingResponseList(bookings);
    }

    /**
     * CANCEL (UPDATE): Cancels a booking and frees the associated seats.
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId , String email) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled.");
        } else if (!booking.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("Only the booking owner can cancel the booking.");
        }

        // Free the seats
        for (Seat seat : booking.getBookedSeats()) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null); // Remove association with the booking
        }
        seatRepository.saveAll(booking.getBookedSeats());

        // Update the booking status
        booking.setStatus(BookingStatus.CANCELLED);


        paymentService.refundPayment(booking.getPayment().getTransactionId());

        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingResponse(updatedBooking);
    }

    /**
     * DELETE: Deletes a booking (for admin purposes) and frees the associated seats.
     */
    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Free the seats before deleting the booking
        for (Seat seat : booking.getBookedSeats()) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null);
        }
        seatRepository.saveAll(booking.getBookedSeats());

        // Now delete the booking itself
        bookingRepository.delete(booking);
    }
}