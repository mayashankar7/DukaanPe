package com.dukaanpe.supplierpurchase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoReorderSuggestionItemResponse {

    private Long productId;
    private String productName;
    private Double currentStock;
    private Double reorderLevel;
    private Double suggestedQuantity;
}

