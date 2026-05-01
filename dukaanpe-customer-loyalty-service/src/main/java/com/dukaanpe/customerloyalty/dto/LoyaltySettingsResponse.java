package com.dukaanpe.customerloyalty.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoyaltySettingsResponse {

    private Long id;
    private Long storeId;
    private Integer pointsPerHundred;
    private Integer pointsToRedeemUnit;
    private Integer redeemValueRupees;
    private BigDecimal silverThreshold;
    private BigDecimal goldThreshold;
    private BigDecimal platinumThreshold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

