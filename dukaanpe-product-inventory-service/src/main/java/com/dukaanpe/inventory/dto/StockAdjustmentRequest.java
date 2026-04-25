package com.dukaanpe.inventory.dto;

import com.dukaanpe.inventory.entity.InventoryTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockAdjustmentRequest {

    @NotNull(message = "Product id is required")
    private Long productId;

    @NotNull(message = "Store id is required")
    private Long storeId;

    @NotNull(message = "Transaction type is required")
    private InventoryTransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;

    private String referenceId;
    private String notes;
    private String createdBy;
}

