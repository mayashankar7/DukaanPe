package com.dukaanpe.customerloyalty.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RedeemLoyaltyPointsRequest {

    @NotNull(message = "customerId is required")
    @Positive(message = "customerId must be positive")
    private Long customerId;

    @NotNull(message = "points is required")
    @Positive(message = "points must be positive")
    private Integer points;

    @Positive(message = "referenceBillId must be positive")
    private Long referenceBillId;

    @Size(max = 300, message = "description must be <= 300 chars")
    private String description;
}

