package com.dukaanpe.customerloyalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class LoyaltySettingsRequest {

    @NotNull(message = "storeId is required")
    @Positive(message = "storeId must be positive")
    private Long storeId;

    @NotNull(message = "pointsPerHundred is required")
    @Min(value = 1, message = "pointsPerHundred must be >= 1")
    private Integer pointsPerHundred;

    @NotNull(message = "pointsToRedeemUnit is required")
    @Min(value = 1, message = "pointsToRedeemUnit must be >= 1")
    private Integer pointsToRedeemUnit;

    @NotNull(message = "redeemValueRupees is required")
    @Min(value = 1, message = "redeemValueRupees must be >= 1")
    private Integer redeemValueRupees;

    @NotNull(message = "silverThreshold is required")
    @Positive(message = "silverThreshold must be positive")
    private BigDecimal silverThreshold;

    @NotNull(message = "goldThreshold is required")
    @Positive(message = "goldThreshold must be positive")
    private BigDecimal goldThreshold;

    @NotNull(message = "platinumThreshold is required")
    @Positive(message = "platinumThreshold must be positive")
    private BigDecimal platinumThreshold;
}

