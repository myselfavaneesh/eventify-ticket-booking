package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.response.AdminDashboardStatsDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.mapper.BookingMapper;
import com.avaneesh.yodha.Eventify.mapper.UserMapper;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import com.avaneesh.yodha.Eventify.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for handling administrative functionalities.
 * Provides methods for fetching dashboard statistics, all users, and all bookings.
 */
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final UserMapper userMapper;
    private final BookingMapper bookingMapper;

    public AdminService(UserRepository userRepository, EventRepository eventRepository, BookingRepository bookingRepository, UserMapper userMapper, BookingMapper bookingMapper) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.userMapper = userMapper;
        this.bookingMapper = bookingMapper;
    }

    /**
     * Gathers and returns key statistics for the admin dashboard.
     *
     * @return A DTO containing total users, events, bookings, and revenue.
     */
    public AdminDashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long totalBookings = bookingRepository.count();
        // Optimized revenue calculation using a dedicated repository query
        double totalRevenue = bookingRepository.sumTotalAmountByStatus(BookingStatus.CONFIRMED);

        return AdminDashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalEvents(totalEvents)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .build();
    }

    /**
     * Retrieves a list of all users in the system.
     *
     * @return A list of UserResponse DTOs.
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    /**
     * Retrieves a list of all bookings in the system.
     *
     * @return A list of BookingResponse DTOs.
     */
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toBookingResponse)
                .toList();
    }
}
