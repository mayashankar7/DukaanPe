package com.dukaanpe.udhar.dto;

import com.dukaanpe.udhar.entity.SettlementMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateSettlementRequest {

    @NotNull(message = "khataCustomerId is required")
    @Positive(message = "khataCustomerId must be positive")
    private Long khataCustomerId;

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotNull(message = "settlementDate is required")
    private LocalDate settlementDate;

    @NotNull(message = "amountSettled is required")
    @Positive(message = "amountSettled must be positive")
    private BigDecimal amountSettled;

    private BigDecimal discountGiven;

    @NotNull(message = "settlementMode is required")
    private SettlementMode settlementMode;

    private String notes;
}

