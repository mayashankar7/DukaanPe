package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompletePaymentRequest {

    @Size(max = 120, message = "upiReference must be <= 120 chars")
    private String upiReference;

    @Pattern(regexp = "^$|^[0-9]{4}$", message = "cardLastFour must be exactly 4 digits")
    private String cardLastFour;
}

