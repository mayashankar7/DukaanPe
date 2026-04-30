package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class GenerateUpiLinkRequest {

    @NotBlank(message = "merchantUpiId is required")
    @Size(max = 120, message = "merchantUpiId must be <= 120 chars")
    private String merchantUpiId;

    @NotBlank(message = "merchantName is required")
    @Size(max = 120, message = "merchantName must be <= 120 chars")
    private String merchantName;

    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @Size(max = 250, message = "description must be <= 250 chars")
    private String description;
}

