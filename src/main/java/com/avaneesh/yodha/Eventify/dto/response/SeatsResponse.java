package com.avaneesh.yodha.Eventify.dto.response;

import com.avaneesh.yodha.Eventify.enums.SeatStatus;
import lombok.Data;

@Data
public class SeatsResponse {
    private Long id;
    private String seatNumber;
    private SeatStatus status;
    private Double seatPricing;
}
