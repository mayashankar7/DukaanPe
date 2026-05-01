package com.dukaanpe.customerloyalty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class EarnLoyaltyPointsRequest {

    @NotNull(message = "customerId is required")
    @Positive(message = "customerId must be positive")
    private Long customerId;

    @NotNull(message = "purchaseAmount is required")
    @Positive(message = "purchaseAmount must be positive")
    private BigDecimal purchaseAmount;

    @Positive(message = "referenceBillId must be positive")
    private Long referenceBillId;

    @Size(max = 300, message = "description must be <= 300 chars")
    private String description;
}

