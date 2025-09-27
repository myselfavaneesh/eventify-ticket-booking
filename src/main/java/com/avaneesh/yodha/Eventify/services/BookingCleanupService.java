package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.entities.Events;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A scheduled service that periodically cleans up expired PENDING bookings.
 */
@Service
public class BookingCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(BookingCleanupService.class);
    private static final int PENDING_BOOKING_EXPIRATION_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;

    public BookingCleanupService(BookingRepository bookingRepository, EventRepository eventRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * A scheduled job that runs every 5 minutes to find and cancel PENDING bookings
     * that have exceeded their expiration time. This prevents seats from being held indefinitely.
     */
    @Scheduled(fixedRate = 300000) // Runs every 5 minutes
    @Transactional
    public void cancelExpiredPendingBookings() {
        logger.info("Running scheduled job to cancel expired PENDING bookings...");

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(PENDING_BOOKING_EXPIRATION_MINUTES);

        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndBookingTimestampBefore(
                BookingStatus.PENDING,
                cutoffTime
        );

        if (expiredBookings.isEmpty()) {
            logger.info("No expired pending bookings found.");
            return;
        }

        logger.info("Found {} expired pending bookings to cancel.", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                // Reclaim the booked seats
                Events event = booking.getEvent();
                event.setBookedSeats(event.getBookedSeats() - booking.getNumberOfSeats());
                eventRepository.save(event); // Save the updated event

                // Mark the booking as cancelled
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking); // Save the updated booking

                logger.info("Successfully cancelled expired booking with ID: {} and reclaimed {} seats for event ID: {}.",
                        booking.getId(), booking.getNumberOfSeats(), event.getId());

            } catch (Exception e) {
                // Log the error but continue processing the rest of the bookings
                logger.error("Error processing cancellation for booking ID: {}.", booking.getId(), e);
            }
        }

        logger.info("Finished processing expired pending bookings.");
    }
}
