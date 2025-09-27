package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.entities.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending emails asynchronously.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Asynchronously sends a booking confirmation email.
     *
     * @param booking The confirmed booking details.
     */
    @Async
    public void sendBookingConfirmationEmail(Booking booking) {
        logger.info("Preparing to send booking confirmation email for booking ID: {}", booking.getId());
        String to = booking.getUser().getEmail();
        String subject = "Booking Confirmation - Your Eventify Ticket";
        String body = String.format(
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
        sendEmail(to, subject, body);
    }

    /**
     * Asynchronously sends a payment failure notification email.
     *
     * @param booking The booking for which the payment failed.
     */
    @Async
    public void sendPaymentFailedEmail(Booking booking) {
        logger.info("Preparing to send payment failure email for booking ID: {}", booking.getId());
        String to = booking.getUser().getEmail();
        String subject = "Payment Failed for Your Eventify Booking";
        String body = String.format(
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
        sendEmail(to, subject, body);
    }

    /**
     * Private helper method to send an email.
     *
     * @param to      The recipient's email address.
     * @param subject The email subject.
     * @param body    The email body text.
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
