package com.dukaanpe.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long storeId;
    private Double currentStock;
    private Double minStockLevel;
    private Double maxStockLevel;
    private Double reorderLevel;
    private String updatedAt;
}

