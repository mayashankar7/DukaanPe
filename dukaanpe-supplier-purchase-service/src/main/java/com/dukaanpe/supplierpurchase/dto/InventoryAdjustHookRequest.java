package com.dukaanpe.supplierpurchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustHookRequest {

    private Long productId;
    private Long storeId;
    private String transactionType;
    private Double quantity;
    private String referenceId;
    private String notes;
    private String createdBy;
}

