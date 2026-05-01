package com.dukaanpe.customerloyalty.dto;

import com.dukaanpe.customerloyalty.entity.CustomerTier;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoyaltySummaryResponse {

    private Long customerId;
    private Integer loyaltyPoints;
    private CustomerTier customerTier;
    private Integer availableForRedeem;
}

