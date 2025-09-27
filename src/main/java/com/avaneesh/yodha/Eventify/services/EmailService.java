package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.entities.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingConfirmationEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("youremail@gmail.com"); // Should be the same as in properties
            message.setTo(booking.getUser().getEmail());
            message.setSubject("Booking Confirmation - Your Eventify Ticket");

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "Thank you for your booking with Eventify!\n\n" +
                            "Here are your booking details:\n" +
                            "Booking ID: %d\n" +
                            "Event: %s\n" +
                            "Venue: %s\n" +
                            "Date: %s\n" +
                            "Total Amount Paid: $%.2f\n\n" +
                            "We look forward to seeing you there!\n\n" +
                            "Best regards,\nThe Eventify Team",
                    booking.getUser().getName(),
                    booking.getId(),
                    booking.getEvent().getName(),
                    booking.getEvent().getVenue(),
                    booking.getEvent().getEventTimestamp().toLocalDate(),
                    booking.getTotalAmount()
            );

            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Booking confirmation email sent to {}", booking.getUser().getEmail());

        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email to {}", booking.getUser().getEmail(), e);
        }
    }

    public void sendPaymentFailedEmail(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("youremail@gmail.com");
            message.setTo(booking.getUser().getEmail());
            message.setSubject("Payment Failed for Your Eventify Booking");

            String emailBody = String.format(
                    "Dear %s,\n\n" +
                            "We're sorry, but the payment for your booking for the event '%s' has failed.\n\n" +
                            "Booking ID: %d\n" +
                            "Unfortunately, your booking has been cancelled, and the seats have been released. Please try booking again.\n\n" +
                            "If you believe this is an error, please contact our support team.\n\n" +
                            "Best regards,\nThe Eventify Team",
                    booking.getUser().getName(),
                    booking.getEvent().getName(),
                    booking.getId()
            );

            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Payment failure email sent to {}", booking.getUser().getEmail());

        } catch (Exception e) {
            logger.error("Failed to send payment failure email to {}", booking.getUser().getEmail(), e);
        }
    }
}