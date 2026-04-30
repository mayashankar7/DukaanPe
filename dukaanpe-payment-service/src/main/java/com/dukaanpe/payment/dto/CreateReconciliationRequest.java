package com.dukaanpe.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateReconciliationRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotNull(message = "reconDate is required")
    private LocalDate reconDate;

    private BigDecimal totalCash;
    private BigDecimal totalUpi;
    private BigDecimal totalCard;
    private BigDecimal cashInHand;
    private String notes;
}

