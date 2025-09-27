package com.avaneesh.yodha.Eventify.dto.request;

import com.avaneesh.yodha.Eventify.enums.PaymentMethod;
import com.avaneesh.yodha.Eventify.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentRequest {
    private String transactionId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
}
