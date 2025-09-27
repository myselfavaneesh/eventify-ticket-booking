package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(BookingCleanupService.class);
    private static final int PENDING_BOOKING_EXPIRATION_MINUTES = 10;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    /**
     * This scheduled task runs every 5 minutes to clean up expired PENDING bookings.
     * The fixedRate is specified in milliseconds (5 minutes = 300,000 ms).
     */
    @Scheduled(fixedRate = 300000)
    public void cancelExpiredPendingBookings() {
        logger.info("Running scheduled job to cancel expired pending bookings...");

        // Calculate the cutoff time (e.g., 10 minutes ago)
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(PENDING_BOOKING_EXPIRATION_MINUTES);

        // Find all bookings that are still PENDING and were created before the cutoff time
        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndBookingTimestampBefore(
                BookingStatus.PENDING,
                cutoffTime
        );

        if (expiredBookings.isEmpty()) {
            logger.info("No expired pending bookings found.");
            return;
        }

        logger.info("Found {} expired pending bookings to cancel.", expiredBookings.size());

        // Iterate through the expired bookings and cancel each one
        for (Booking booking : expiredBookings) {
            try {
                logger.info("Cancelling booking with ID: {}", booking.getId());
                bookingService.cancelBooking(booking.getId());
            } catch (Exception e) {
                logger.error("Error cancelling booking with ID: {}", booking.getId(), e);
            }
        }

        logger.info("Finished cancelling expired pending bookings.");
    }
}