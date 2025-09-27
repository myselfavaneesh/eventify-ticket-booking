package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.request.BookingRequestDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.entities.Seat;
import com.avaneesh.yodha.Eventify.entities.Users;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.enums.SeatStatus;
import com.avaneesh.yodha.Eventify.exception.ResourceNotFoundException;
import com.avaneesh.yodha.Eventify.mapper.BookingMapper;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import com.avaneesh.yodha.Eventify.repository.SeatRepository;
import com.avaneesh.yodha.Eventify.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for handling booking-related operations such as creation, retrieval, and cancellation.
 */
@Service
public class BookingService {

    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final PaymentService paymentService;

    public BookingService(UserRepository userRepository, SeatRepository seatRepository,
                          EventRepository eventRepository, BookingRepository bookingRepository,
                          BookingMapper bookingMapper, PaymentService paymentService) {
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.paymentService = paymentService;
    }

    /**
     * Creates a new booking, locks the selected seats, and updates the event's seat count.
     *
     * @param email           The email of the user making the booking.
     * @param requestBooking  The DTO containing booking request details (event ID and seat IDs).
     * @return A DTO representing the newly created PENDING booking.
     * @throws ResourceNotFoundException if the user, event, or seats are not found.
     * @throws IllegalStateException if the booking request is invalid (e.g., seats unavailable, booking window closed).
     */
    @Transactional
    public BookingResponse createBooking(String email, BookingRequestDTO requestBooking) {
        Users user = userRepository.getUsersByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Events event = findAndValidateEventForBooking(requestBooking.getEventId());
        List<Seat> requestedSeats = findAndValidateSeatsForBooking(requestBooking.getSeatIds(), event);

        // Update event's booked seat count
        event.setBookedSeats(event.getBookedSeats() + requestedSeats.size());
        eventRepository.save(event);

        double totalAmount = requestedSeats.stream().mapToDouble(Seat::getSeatPricing).sum();

        Booking newBooking = buildAndSaveBooking(user, event, requestedSeats, totalAmount);

        // Mark seats as LOCKED and associate them with the new booking
        requestedSeats.forEach(seat -> {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setBooking(newBooking);
        });
        seatRepository.saveAll(requestedSeats);

        return bookingMapper.toBookingResponse(newBooking);
    }

    /**
     * Retrieves a single booking by its ID.
     *
     * @param bookingId The ID of the booking to retrieve.
     * @return A DTO representing the booking.
     */
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return bookingMapper.toBookingResponse(booking);
    }

    /**
     * Retrieves all bookings made by a specific user.
     *
     * @param email The email of the user.
     * @return A list of DTOs representing the user's bookings.
     */
    public List<BookingResponse> getAllBookingsForUser(String email) {
        Users user = userRepository.getUsersByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        List<Booking> bookings = bookingRepository.findAllByUser(user);
        return bookingMapper.toBookingResponseList(bookings);
    }

    /**
     * Cancels a booking, releases the associated seats, updates the event's seat count, and processes a refund if applicable.
     *
     * @param bookingId The ID of the booking to cancel.
     * @param email     The email of the user attempting the cancellation, for ownership verification.
     * @return A DTO representing the updated, CANCELLED booking.
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String email) {
        Booking booking = findAndValidateBookingForCancellation(bookingId, email);

        // Release seats and update event seat count
        releaseSeatsForBooking(booking);

        // Process refund only if the booking was confirmed and paid for
        if (booking.getStatus() == BookingStatus.CONFIRMED && booking.getPayment() != null) {
            paymentService.refundPayment(booking.getPayment().getTransactionId());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingResponse(updatedBooking);
    }

    /**
     * Deletes a booking and releases its associated seats. Intended for administrative use.
     *
     * @param bookingId The ID of the booking to delete.
     */
    @Transactional
    public void deleteBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        releaseSeatsForBooking(booking);
        bookingRepository.delete(booking);
    }

    // --- Private Helper Methods ---

    private Events findAndValidateEventForBooking(Long eventId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        LocalDateTime bookingCutoffTime = event.getEventTimestamp().minusMinutes(15);
        if (LocalDateTime.now().isAfter(bookingCutoffTime)) {
            throw new IllegalStateException("Booking is closed. Bookings cannot be made within 15 minutes of the event start time.");
        }
        return event;
    }

    private List<Seat> findAndValidateSeatsForBooking(List<Long> seatIds, Events event) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalStateException("Seat selection cannot be empty.");
        }

        List<Seat> requestedSeats = seatRepository.findAllById(seatIds);
        if (requestedSeats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("One or more requested seats could not be found.");
        }

        for (Seat seat : requestedSeats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is not available.");
            }
            if (!seat.getEvent().getId().equals(event.getId())) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " does not belong to the requested event.");
            }
        }

        if (event.getAvailableSeats() < requestedSeats.size()) {
            throw new IllegalStateException("Not enough available seats for this booking. Requested: " + requestedSeats.size() + ", Available: " + event.getAvailableSeats());
        }

        return requestedSeats;
    }

    private Booking buildAndSaveBooking(Users user, Events event, List<Seat> seats, double totalAmount) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setBookingTimestamp(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(totalAmount);
        booking.setNumberOfSeats(seats.size());
        booking.setBookedSeats(seats);
        return bookingRepository.save(booking);
    }

    private Booking findAndValidateBookingForCancellation(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new IllegalStateException("You are not authorized to cancel this booking.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled.");
        }
        return booking;
    }

    private void releaseSeatsForBooking(Booking booking) {
        List<Seat> seatsToRelease = booking.getBookedSeats();
        if (seatsToRelease != null && !seatsToRelease.isEmpty()) {
            for (Seat seat : seatsToRelease) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBooking(null);
            }
            seatRepository.saveAll(seatsToRelease);

            Events event = booking.getEvent();
            event.setBookedSeats(event.getBookedSeats() - seatsToRelease.size());
            eventRepository.save(event);
        }
    }
}
