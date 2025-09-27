package com.avaneesh.yodha.Eventify.entities;

import com.avaneesh.yodha.Eventify.enums.PaymentMethod;
import com.avaneesh.yodha.Eventify.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payments")
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private LocalDateTime paymentDate;
}