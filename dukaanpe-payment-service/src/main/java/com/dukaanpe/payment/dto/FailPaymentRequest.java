package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FailPaymentRequest {

    @NotBlank(message = "reason is required")
    @Size(max = 250, message = "reason must be <= 250 chars")
    private String reason;
}

