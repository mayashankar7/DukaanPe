package com.dukaanpe.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class InventoryUpdateRequest {

    @NotNull(message = "Current stock is required")
    @PositiveOrZero(message = "Current stock must be zero or positive")
    private Double currentStock;

    @PositiveOrZero(message = "Min stock level must be zero or positive")
    private Double minStockLevel;

    @PositiveOrZero(message = "Max stock level must be zero or positive")
    private Double maxStockLevel;

    @PositiveOrZero(message = "Reorder level must be zero or positive")
    private Double reorderLevel;
}

