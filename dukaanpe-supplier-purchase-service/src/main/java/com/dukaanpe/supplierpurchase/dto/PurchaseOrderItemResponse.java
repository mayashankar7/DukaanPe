package com.dukaanpe.supplierpurchase.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseOrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Double quantityOrdered;
    private Double quantityReceived;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal gstRate;
    private BigDecimal totalAmount;
}

