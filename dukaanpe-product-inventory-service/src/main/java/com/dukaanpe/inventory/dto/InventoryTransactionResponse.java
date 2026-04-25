package com.dukaanpe.inventory.dto;

import com.dukaanpe.inventory.entity.InventoryTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long storeId;
    private InventoryTransactionType transactionType;
    private Double quantity;
    private Double previousStock;
    private Double newStock;
    private String referenceId;
    private String notes;
    private String createdBy;
    private String createdAt;
}

