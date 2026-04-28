package com.dukaanpe.supplierpurchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PurchaseOrderItemRequest {

    private Long productId;

    @NotBlank(message = "productName is required")
    private String productName;

    @NotNull(message = "quantityOrdered is required")
    @Positive(message = "quantityOrdered must be positive")
    private Double quantityOrdered;

    @NotBlank(message = "unit is required")
    private String unit;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "unitPrice must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "gstRate is required")
    @DecimalMin(value = "0.0", message = "gstRate must be >= 0")
    private BigDecimal gstRate;
}

