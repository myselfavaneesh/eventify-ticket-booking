package com.avaneesh.yodha.Eventify.services;

import com.avaneesh.yodha.Eventify.dto.response.AdminDashboardStatsDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.entities.Booking;
import com.avaneesh.yodha.Eventify.enums.BookingStatus;
import com.avaneesh.yodha.Eventify.mapper.BookingMapper;
import com.avaneesh.yodha.Eventify.mapper.UserMapper;
import com.avaneesh.yodha.Eventify.repository.BookingRepository;
import com.avaneesh.yodha.Eventify.repository.EventRepository;
import com.avaneesh.yodha.Eventify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BookingMapper bookingMapper;

    public AdminDashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long totalBookings = bookingRepository.count();

        // Calculate revenue only from CONFIRMED bookings
        double totalRevenue = bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .mapToDouble(Booking::getTotalAmount)
                .sum();

        return AdminDashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalEvents(totalEvents)
                .totalBookings(totalBookings)
                .totalRevenue(totalRevenue)
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toBookingResponse)
                .toList();
    }
}