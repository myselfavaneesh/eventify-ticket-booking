package com.avaneesh.yodha.Eventify.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "events")
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int seatsPerRow;

    @Column(nullable = false)
    private int bookedSeats = 0;

    // One Event has Many Seats
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();

    // One Event has Many Bookings
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    /**
     * Calculates the number of available seats.
     * @return The number of seats that are not yet booked.
     */
    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }
}
