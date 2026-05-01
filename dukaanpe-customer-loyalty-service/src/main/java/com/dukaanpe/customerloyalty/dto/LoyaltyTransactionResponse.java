package com.dukaanpe.customerloyalty.dto;

import com.dukaanpe.customerloyalty.entity.LoyaltyTransactionType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoyaltyTransactionResponse {

    private Long id;
    private Long customerId;
    private Long storeId;
    private LoyaltyTransactionType transactionType;
    private Integer points;
    private Integer runningBalance;
    private Long referenceBillId;
    private String description;
    private LocalDateTime createdAt;
}

