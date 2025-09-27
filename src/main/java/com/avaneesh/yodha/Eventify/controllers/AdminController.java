package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.response.AdminDashboardStatsDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.services.AdminService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDTO>> getDashboardStats() {
        AdminDashboardStatsDTO stats = adminService.getDashboardStats();
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard statistics retrieved successfully.", stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse<>(true, "All users retrieved successfully.", users));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = adminService.getAllBookings();
        return ResponseEntity.ok(new ApiResponse<>(true, "All bookings retrieved successfully.", bookings));
    }
}