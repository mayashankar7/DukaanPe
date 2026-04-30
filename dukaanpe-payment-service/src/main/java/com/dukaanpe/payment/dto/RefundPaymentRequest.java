package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RefundPaymentRequest {

    @Positive(message = "refundAmount must be positive")
    private BigDecimal refundAmount;

    @NotBlank(message = "reason is required")
    @Size(max = 250, message = "reason must be <= 250 chars")
    private String reason;
}

