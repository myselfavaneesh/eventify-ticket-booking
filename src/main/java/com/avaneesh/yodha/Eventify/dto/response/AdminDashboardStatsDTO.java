package com.avaneesh.yodha.Eventify.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardStatsDTO {
    private long totalUsers;
    private long totalEvents;
    private long totalBookings;
    private double totalRevenue;
}