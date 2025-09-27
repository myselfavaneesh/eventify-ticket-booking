package com.avaneesh.yodha.Eventify.controllers;

import com.avaneesh.yodha.Eventify.dto.response.AdminDashboardStatsDTO;
import com.avaneesh.yodha.Eventify.dto.response.BookingResponse;
import com.avaneesh.yodha.Eventify.dto.response.UserResponse;
import com.avaneesh.yodha.Eventify.services.AdminService;
import com.avaneesh.yodha.Eventify.utils.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for handling administrative tasks and data retrieval.
 * Provides endpoints for accessing dashboard statistics, user lists, and booking information.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints for administrative tasks")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Retrieves aggregated statistics for the admin dashboard.
     *
     * @return A response entity containing the dashboard statistics.
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDTO>> getDashboardStats() {
        AdminDashboardStatsDTO stats = adminService.getDashboardStats();
        ApiResponse<AdminDashboardStatsDTO> response = new ApiResponse<>(true, "Dashboard statistics retrieved successfully.", stats);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of all users in the system.
     *
     * @return A response entity containing the list of all users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        ApiResponse<List<UserResponse>> response = new ApiResponse<>(true, "All users retrieved successfully.", users);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a list of all bookings made in the system.
     *
     * @return A response entity containing the list of all bookings.
     */
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = adminService.getAllBookings();
        ApiResponse<List<BookingResponse>> response = new ApiResponse<>(true, "All bookings retrieved successfully.", bookings);
        return ResponseEntity.ok(response);
    }
}
